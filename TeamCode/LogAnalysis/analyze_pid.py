import pandas as pd
import matplotlib.pyplot as plt
import argparse
import os
import sys
import io
import numpy as np

def parse_log_file(file_path):
    """
    Parses the DataLogger CSV file using pandas.
    """
    try:
        # Strategy: Read the file to find the header line index
        header_line_index = -1
        with open(file_path, 'r') as f:
            lines = f.readlines()
        
        for i, line in enumerate(lines):
            if "Timestamp" in line and "," in line:
                header_line_index = i
                break
        
        if header_line_index == -1:
            print(f"Error: Could not find header row with 'Timestamp' in {file_path}")
            return None

        # Keep header, and filter comments from the rest
        header = lines[header_line_index]
        body = [line for line in lines[header_line_index+1:] if not line.strip().startswith('#')]
        
        df = pd.read_csv(io.StringIO(''.join([header] + body)))
        
        # Robust rename: handle "# Timestamp", "#Timestamp", "Timestamp"
        df.rename(columns=lambda x: x.replace('#', '').strip(), inplace=True)
        return df
    except pd.errors.EmptyDataError:
        print(f"Error: File {file_path} is empty.")
        return None
    except Exception as e:
        print(f"Error parsing CSV: {e}")
        return None

def calculate_step_metrics(df, target_col, actual_col, time_col):
    """
    Calculates transient response metrics for a step input.
    Assumes the largest target value represents the step.
    """
    try:
        # Find the steady state target (max absolute value)
        target_val = df[target_col].abs().max()
        if target_val == 0: return None
        
        # Filter for the step portion (where target is > 90% of max)
        # This avoids the initial 0 period
        step_mask = df[target_col].abs() > (target_val * 0.90)
        if not step_mask.any(): return None
        
        step_data = df[step_mask].copy()
        start_time = step_data[time_col].iloc[0]
        
        # Normalize time
        times = step_data[time_col] - start_time
        actuals = step_data[actual_col]
        errors = step_data[target_col] - actuals
        
        # 1. Overshoot %
        # (Max - Target) / Target
        # Handle direction
        if df[target_col].mean() > 0:
            max_val = actuals.max()
            overshoot_pct = max(0, (max_val - target_val) / target_val * 100)
        else:
            min_val = actuals.min() # e.g. -350 vs -300
            overshoot_pct = max(0, (abs(min_val) - target_val) / target_val * 100)

        # 2. Settling Time (within 2% of target)
        threshold = 0.02 * target_val
        # Find the last time the error was OUTSIDE the band
        outside_band = step_data[errors.abs() > threshold]
        
        if outside_band.empty:
            settling_time = 0.0
        else:
            # Time of the last point outside the band
            settling_time = outside_band[time_col].iloc[-1] - start_time

        # 3. Steady State Error (last 20% of the step duration)
        duration = times.iloc[-1]
        steady_state_start = duration * 0.8
        ss_mask = times > steady_state_start
        
        if ss_mask.any():
            ss_error = errors[ss_mask].mean()
        else:
            ss_error = 0.0
            
        return {
            "target": target_val,
            "overshoot_pct": overshoot_pct,
            "settling_time": settling_time,
            "ss_error": ss_error,
            "ss_error_pct": (ss_error / target_val) * 100
        }
    except Exception as e:
        print(f"Warning: Could not calculate step metrics: {e}")
        return None

def get_tuning_advice(metrics):
    advice = []
    if metrics is None:
        return ["Could not analyze step response automatically."]

    # Overshoot
    if metrics['overshoot_pct'] > 10:
        advice.append("High Overshoot (>10%): Decrease kP or Increase kD.")
    elif metrics['overshoot_pct'] > 5:
        advice.append("Moderate Overshoot (>5%): Slight decrease in kP might help.")
        
    # Settling Time / Response Speed
    # This is relative, but if it takes > 1.5s to settle, it's usually slow for FTC motors
    if metrics['settling_time'] > 1.5:
        advice.append(f"Slow Settling Time ({metrics['settling_time']:.2f}s): Increase kP or tune Feedforward (kV).")
        
    # Steady State Error
    if abs(metrics['ss_error_pct']) > 2.0:
        advice.append(f"Steady-State Error ({metrics['ss_error_pct']:.1f}%): Increase kI or adjust Feedforward (kV/kS).")
        
    if not advice:
        advice.append("Performance looks good! (Overshoot < 5%, SS Error < 2%)")
        
    return advice

def analyze_pid(file_path, save_plot=False):
    print(f"Analyzing {file_path}...")
    df = parse_log_file(file_path)
    if df is None:
        return

    # Check for Timestamp
    if 'Timestamp' not in df.columns:
        print("Error: Missing 'Timestamp' column.")
        return

    # Check for TargetTPS
    if 'TargetTPS' not in df.columns:
        print("Error: Missing 'TargetTPS' column.")
        return

    # Find all "Actual" TPS columns (columns ending in TPS that are not TargetTPS)
    tps_cols = [c for c in df.columns if c.endswith('TPS') and c != 'TargetTPS']
    
    if not tps_cols:
        print("Error: No actual TPS columns found (looking for columns ending in 'TPS').")
        print(f"Available columns: {df.columns.tolist()}")
        return

    print(f"Found motor columns: {tps_cols}")

    # --- Plotting Setup ---
    plt.figure(figsize=(12, 10))
    
    # Check if ANY motor has power columns to decide on 3rd subplot
    # We do a quick check for likely power columns
    has_power = False
    for tps_col in tps_cols:
        prefix = tps_col[:-3]
        if f"{prefix}Power" in df.columns or "TotalPower" in df.columns:
            has_power = True
            break
            
    num_plots = 3 if has_power else 2

    # 1. Velocity (Target) - Plot once
    plt.subplot(num_plots, 1, 1)
    plt.plot(df['Timestamp'], df['TargetTPS'], label='Target TPS', color='black', linestyle='--', linewidth=2)

    colors = ['blue', 'orange', 'green', 'red', 'purple']

    for i, tps_col in enumerate(tps_cols):
        # Determine prefix (e.g., "Right" from "RightTPS", or "" from "ActualTPS")
        prefix = tps_col[:-3] 
        color = colors[i % len(colors)]
        
        print(f"\n--- Analyzing Motor: {tps_col} ---")
        
        # Determine associated columns
        error_col = f"{prefix}Error"
        power_col = f"{prefix}Power"
        
        # Fallback for single motor legacy naming
        if prefix == "Actual":
            if "TotalPower" in df.columns: power_col = "TotalPower"
            if "Error" in df.columns: error_col = "Error"
        elif prefix == "": # Just "TPS" ? Unlikely but possible
             if "TotalPower" in df.columns: power_col = "TotalPower"

        # Calculate Error if missing
        if error_col not in df.columns:
            print(f"Note: '{error_col}' not found, calculating from Target - Actual")
            df[error_col] = df['TargetTPS'] - df[tps_col]
        
        # Metrics
        rmse = (df[error_col] ** 2).mean() ** 0.5
        max_error = df[error_col].abs().max()
        
        print(f"RMSE: {rmse:.2f}")
        print(f"Max Absolute Error: {max_error:.2f}")

        # --- Advanced Analysis & Recommendations ---
        step_metrics = calculate_step_metrics(df, 'TargetTPS', tps_col, 'Timestamp')
        if step_metrics:
            print(f"  -> Overshoot: {step_metrics['overshoot_pct']:.1f}%")
            print(f"  -> Settling Time (2%): {step_metrics['settling_time']:.2f}s")
            print(f"  -> Steady-State Error: {step_metrics['ss_error']:.2f} ({step_metrics['ss_error_pct']:.1f}%)")
            
            advice = get_tuning_advice(step_metrics)
            print("  -> Recommendations:")
            for tip in advice:
                print(f"     * {tip}")
        else:
            print("  -> Could not detect clear step response for detailed analysis.")
        
        sys.stdout.flush() # Ensure output is visible before plotting

        # --- Add to Plots ---
        
        # 1. Velocity
        plt.subplot(num_plots, 1, 1)
        plt.plot(df['Timestamp'], df[tps_col], label=f'{tps_col}', color=color, alpha=0.8)
        
        # 2. Error
        plt.subplot(num_plots, 1, 2)
        plt.plot(df['Timestamp'], df[error_col], label=f'{prefix}Error', color=color, alpha=0.8)
        
        # 3. Power
        if has_power:
            plt.subplot(num_plots, 1, 3)
            # Check if specific power col exists, else try TotalPower if it's the only one
            if power_col in df.columns:
                plt.plot(df['Timestamp'], df[power_col], label=f'{prefix}Power', color=color, alpha=0.8)
            elif "TotalPower" in df.columns and len(tps_cols) == 1:
                 plt.plot(df['Timestamp'], df["TotalPower"], label='TotalPower', color=color, alpha=0.8)

    # --- Finalize Plots ---
    plt.subplot(num_plots, 1, 1)
    plt.title(f'PID Response: {os.path.basename(file_path)}')
    plt.ylabel('Velocity (TPS)')
    plt.legend()
    plt.grid(True)

    plt.subplot(num_plots, 1, 2)
    plt.axhline(y=0, color='black', linestyle='-', alpha=0.3)
    plt.ylabel('Error (TPS)')
    plt.legend()
    plt.grid(True)

    if has_power:
        plt.subplot(num_plots, 1, 3)
        plt.ylabel('Power (-1 to 1)')
        plt.xlabel('Time (s)')
        plt.legend()
        plt.grid(True)
    else:
        plt.subplot(num_plots, 1, 2)
        plt.xlabel('Time (s)')
            
    plt.tight_layout()
    
    if save_plot:
        img_path = file_path.replace('.csv', '.png')
        plt.savefig(img_path)
        print(f"Plot saved to {img_path}")
        
        # Open the file automatically in the default OS viewer
        try:
            if os.name == 'nt': # Windows
                os.startfile(img_path)
            elif sys.platform == 'darwin': # macOS
                os.system(f'open "{img_path}"')
            else: # Linux
                os.system(f'xdg-open "{img_path}"')
        except Exception as e:
            print(f"Warning: Could not open plot automatically: {e}")

    else:
        # Flush stdout to ensure all text is printed before the window blocks
        sys.stdout.flush()
        plt.show()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Analyze PID Performance from DataLogger CSV')
    parser.add_argument('file', nargs='?', help='Path to the CSV log file')
    parser.add_argument('--save', action='store_true', help='Save plot to PNG instead of showing it')
    args = parser.parse_args()

    if args.file:
        analyze_pid(args.file, args.save)
    else:
        # Find the latest CSV
        files = [f for f in os.listdir('.') if f.endswith('.csv')]
        if not files:
            log_dir = os.path.join('..', '..', 'logs')
            if os.path.exists(log_dir):
                files = [os.path.join(log_dir, f) for f in os.listdir(log_dir) if f.endswith('.csv')]
        
        if files:
            latest_file = max(files, key=os.path.getmtime)
            print(f"No file specified. Using latest log: {latest_file}")
            analyze_pid(latest_file, args.save)
        else:
            print("No CSV log files found.")
