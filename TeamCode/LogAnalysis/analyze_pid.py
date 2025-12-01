import pandas as pd
import matplotlib.pyplot as plt
import argparse
import os
import sys
import io

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
        
        # Ensure sorted by Timestamp for binary search in hover tool
        df.sort_values('Timestamp', inplace=True)
        
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

    # Check if ANY motor has power columns to decide on 3rd subplot
    # We do a quick check for likely power columns
    has_power = False
    for tps_col in tps_cols:
        prefix = tps_col[:-3]
        if f"{prefix}Power" in df.columns or "TotalPower" in df.columns:
            has_power = True
            break
            
    num_plots = 3 if has_power else 2

    # --- Plotting Setup ---
    # Use subplots with sharex=True to synchronize zooming/panning
    fig, axes = plt.subplots(num_plots, 1, figsize=(12, 10), sharex=True)
    if num_plots == 1: axes = [axes] # Handle single plot case if it ever happens

    # Define Color Map (User Preference: Green=Target, Blue=Left, Yellow=Right)
    color_map = {}
    # Default palette: Blue, Yellow, Purple, Red, Cyan
    palette = ['#1f77b4', '#e3c800', '#9467bd', '#d62728', '#17becf']
    
    # Heuristic for Left/Right
    left_col = next((c for c in tps_cols if 'Left' in c), None)
    right_col = next((c for c in tps_cols if 'Right' in c), None)
    
    if left_col and right_col and len(tps_cols) == 2:
        color_map[left_col] = '#1f77b4' # Blue
        color_map[right_col] = '#e3c800' # Yellow
    else:
        for i, col in enumerate(tps_cols):
            color_map[col] = palette[i % len(palette)]

    # 1. Velocity (Target) - Plot once
    plt.sca(axes[0])
    plt.plot(df['Timestamp'], df['TargetTPS'], label='Target TPS', color='#2ca02c', linestyle='--', linewidth=2, alpha=0.9) # Green Target

    # --- Readiness Analysis ---
    # Define Tolerance (User can adjust this in code or via args later, default 5%)
    TOLERANCE_PCT = 0.05
    
    # Calculate "Ready" state: Both motors within tolerance
    # We need to check if we have 2 motors for this specific logic, otherwise just check individual
    ready_mask = pd.Series(True, index=df.index)
    
    for tps_col in tps_cols:
        prefix = tps_col[:-3]
        error_col = f"{prefix}Error"
        if error_col not in df.columns:
             df[error_col] = df['TargetTPS'] - df[tps_col]
        
        # Check if error is within tolerance relative to Target
        # Avoid division by zero for small targets
        target_abs = df['TargetTPS'].abs()
        tolerance_abs = target_abs * TOLERANCE_PCT
        # Use a minimum threshold for low speeds (e.g. 50 TPS) to avoid noise
        tolerance_abs = tolerance_abs.clip(lower=50) 
        
        in_range = df[error_col].abs() <= tolerance_abs
        ready_mask = ready_mask & in_range

    # Highlight "Ready to Shoot" Zones
    # We only care when Target is active (> 100 TPS)
    active_ready_mask = ready_mask & (df['TargetTPS'].abs() > 100)
    
    # Fill green for Ready
    plt.fill_between(df['Timestamp'], df['TargetTPS'].min(), df['TargetTPS'].max(), 
                     where=active_ready_mask, 
                     color='green', alpha=0.1, label='Ready to Shoot (In Range)')

    # --- Premature/Oscillation Detection ---
    # If "Ready" but Velocity is changing rapidly (Acceleration is high), it's unstable/premature
    # Calculate Acceleration (Derivative of Velocity)
    dt = df['Timestamp'].diff().fillna(0.01) # Avoid div by zero
    
    unstable_mask = pd.Series(False, index=df.index)
    
    for tps_col in tps_cols:
        velocity = df[tps_col]
        acceleration = velocity.diff() / dt
        
        # Define Stability Threshold (e.g., changing by more than 10% of Target per second?)
        # Or a fixed value. Let's try 500 TPS/s as a heuristic for "swinging"
        # Better: Relative to target. If Accel > Target * 2, it's moving fast.
        accel_threshold = df['TargetTPS'].abs() * 2.0 
        accel_threshold = accel_threshold.clip(lower=500)
        
        is_unstable = acceleration.abs() > accel_threshold
        unstable_mask = unstable_mask | is_unstable

    # "Risky Shot" = Ready AND Unstable
    risky_mask = active_ready_mask & unstable_mask
    
    if risky_mask.any():
        plt.fill_between(df['Timestamp'], df['TargetTPS'].min(), df['TargetTPS'].max(), 
                         where=risky_mask, 
                         color='orange', alpha=0.3, label='Unstable/Premature (High Accel)')
        print("\nWARNING: Detected potential premature shooting zones (Orange).")
        print("The motors are passing through the target range but moving too fast to be stable.")

    motor_plot_data = [] # Store data for discrepancy fill

    for i, tps_col in enumerate(tps_cols):
        # Determine prefix (e.g., "Right" from "RightTPS", or "" from "ActualTPS")
        prefix = tps_col[:-3] 
        color = color_map.get(tps_col, palette[i % len(palette)])
        
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
        plt.sca(axes[0])
        plt.plot(df['Timestamp'], df[tps_col], label=f'{tps_col}', color=color, linewidth=1.5, alpha=0.9)
        motor_plot_data.append((tps_col, df[tps_col]))
        
        # 2. Error
        plt.sca(axes[1])
        plt.plot(df['Timestamp'], df[error_col], label=f'{prefix}Error', color=color, alpha=0.8)
        
        # 3. Power
        if has_power:
            plt.sca(axes[2])
            # Check if specific power col exists, else try TotalPower if it's the only one
            if power_col in df.columns:
                plt.plot(df['Timestamp'], df[power_col], label=f'{prefix}Power', color=color, alpha=0.8)
                # Highlight Saturation for this motor
                plt.fill_between(df['Timestamp'], -1, 1, 
                                 where=(df[power_col].abs() > 0.95), 
                                 color='red', alpha=0.05, label='_nolegend_') # Light red background for saturation
            elif "TotalPower" in df.columns and len(tps_cols) == 1:
                 plt.plot(df['Timestamp'], df["TotalPower"], label='TotalPower', color=color, alpha=0.8)
                 plt.fill_between(df['Timestamp'], -1, 1, 
                                 where=(df["TotalPower"].abs() > 0.95), 
                                 color='red', alpha=0.05, label='_nolegend_')

    # --- Finalize Plots ---
    
    # Highlight Discrepancy if exactly 2 motors
    if len(motor_plot_data) == 2:
        # Calculate Discrepancy Metrics
        col1_name, col1_data = motor_plot_data[0]
        col2_name, col2_data = motor_plot_data[1]
        
        discrepancy = (col1_data - col2_data).abs()
        max_diff = discrepancy.max()
        mean_diff = discrepancy.mean()
        
        # Filter for when target is non-zero to avoid idle noise
        active_mask = df['TargetTPS'].abs() > 10
        if active_mask.any():
            active_diff = discrepancy[active_mask]
            max_active_diff = active_diff.max()
            mean_active_diff = active_diff.mean()
            
            # Calculate % difference relative to target
            # Avoid division by zero
            target_active = df.loc[active_mask, 'TargetTPS'].abs()
            pct_diff = (active_diff / target_active) * 100
            max_pct_diff = pct_diff.max()
            mean_pct_diff = pct_diff.mean()
            
            print(f"\n--- Synchronization Analysis ({col1_name} vs {col2_name}) ---")
            print(f"Max Discrepancy (Active): {max_active_diff:.2f} TPS")
            print(f"Mean Discrepancy (Active): {mean_active_diff:.2f} TPS")
            print(f"Max % Difference: {max_pct_diff:.1f}%")
            print(f"Mean % Difference: {mean_pct_diff:.1f}%")
            
            if max_pct_diff > 5.0:
                print("WARNING: Significant discrepancy detected (>5%). This may cause curved shots.")
            elif max_pct_diff > 2.0:
                print("Note: Moderate discrepancy detected (2-5%).")
            else:
                print("Good synchronization (<2% difference).")

        plt.sca(axes[0])
        plt.fill_between(df['Timestamp'], col1_data, col2_data, 
                         color='red', alpha=0.1, label='Discrepancy')

    plt.sca(axes[0])
    plt.title(f'Velocity Tracking: {os.path.basename(file_path)}\nGreen Zone = Ready to Shoot | Orange Zone = Unstable/Premature')
    plt.ylabel('Velocity (TPS)')
    plt.legend(loc='best')
    plt.grid(True, which='both', linestyle='--', alpha=0.7)

    # Finalize Error Plot (Actionable Insights)
    plt.sca(axes[1])
    plt.axhline(y=0, color='black', linestyle='-', alpha=0.5)
    
    # Add Tolerance Bands (Actionable: Is error within 5%?)
    # Re-calculate tolerance for visualization
    tolerance_5pct = df['TargetTPS'].abs() * 0.05
    tolerance_5pct = tolerance_5pct.clip(lower=50)
    
    plt.fill_between(df['Timestamp'], tolerance_5pct, -tolerance_5pct, 
                     color='green', alpha=0.1, label='Acceptable Range (5%)')
    
    plt.ylabel('Error (TPS)')
    plt.title('Error & Recovery (Goal: Stay in Green Band)')
    plt.legend(loc='best')
    plt.grid(True, which='both', linestyle='--', alpha=0.7)

    if has_power:
        plt.sca(axes[2])
        plt.ylabel('Power (-1 to 1)')
        plt.title('Motor Effort (Goal: Smooth Control, Avoid Saturation)')
        plt.xlabel('Time (s)')
        plt.axhline(y=1, color='red', linestyle=':', alpha=0.5)
        plt.axhline(y=-1, color='red', linestyle=':', alpha=0.5)
        plt.axhline(y=0, color='black', linestyle='-', alpha=0.3)
        
        # Add a dummy fill for the legend to explain the red background
        plt.fill_between([], [], [], color='red', alpha=0.1, label='Saturation (>95%)')
        
        plt.legend(loc='best')
        plt.grid(True, which='both', linestyle='--', alpha=0.7)
    else:
        plt.sca(axes[1])
        plt.xlabel('Time (s)')
            
    plt.tight_layout()

    # --- Interactive Cursor (Optimized) ---
    # Use a vertical line that snaps to data points to avoid constant redraws
    
    # Create vertical lines for each subplot (initially invisible)
    vlines = [ax.axvline(x=df['Timestamp'].iloc[0], color='gray', linestyle='--', alpha=0.8, visible=False) for ax in axes]
    
    # Annotation box on the first subplot
    annot = axes[0].annotate("", xy=(0,0), xytext=(10,10), textcoords="offset points",
                            bbox=dict(boxstyle="round", fc="w", alpha=0.9),
                            arrowprops=dict(arrowstyle="->"), zorder=10)
    annot.set_visible(False)
    
    # Store last index to prevent unnecessary redraws
    last_idx = [None]

    def update_annot(idx):
        x = df['Timestamp'].iloc[idx]
        
        # Build text with info from all motors
        text_lines = [f"Time: {x:.3f}s", f"Target: {df['TargetTPS'].iloc[idx]:.0f}"]
        
        for tps_col in tps_cols:
            val = df[tps_col].iloc[idx]
            text_lines.append(f"{tps_col}: {val:.0f}")
            
            # Add Error info
            prefix = tps_col[:-3]
            err_col = f"{prefix}Error"
            if err_col in df.columns:
                text_lines.append(f"{prefix}Err: {df[err_col].iloc[idx]:.0f}")
                
        annot.xy = (x, df['TargetTPS'].iloc[idx])
        annot.set_text("\n".join(text_lines))

    def hover(event):
        # Performance: Only show if Left Ctrl is pressed AND mouse is inside axes
        should_show = (event.key == 'control') and (event.inaxes in axes)

        if not should_show:
            if annot.get_visible():
                annot.set_visible(False)
                for vl in vlines: vl.set_visible(False)
                fig.canvas.draw_idle()
            return

        # Find nearest timestamp index
        # Binary search for performance O(log N)
        target_x = event.xdata
        timestamps = df['Timestamp'].values
        idx = timestamps.searchsorted(target_x)
        
        # Clamp index and find closest
        if idx >= len(timestamps): idx = len(timestamps) - 1
        if idx > 0:
            # Check which is closer: idx or idx-1
            if abs(timestamps[idx] - target_x) > abs(timestamps[idx-1] - target_x):
                idx = idx - 1
        
        # Only redraw if index changed (Major Performance Optimization)
        if idx != last_idx[0]:
            last_idx[0] = idx
            
            # Update position
            x_pos = timestamps[idx]
            for vl in vlines:
                vl.set_xdata([x_pos, x_pos])
                vl.set_visible(True)
            
            update_annot(idx)
            
            # Smart positioning to avoid overlap with title/top
            # Check if we are in the top 20% of the visible y-axis
            ylim = axes[0].get_ylim()
            y_range = ylim[1] - ylim[0]
            y_val = df['TargetTPS'].iloc[idx]
            
            # If y_val is high up, move tooltip down-right instead of up-right
            if (y_val - ylim[0]) / y_range > 0.8:
                 annot.xytext = (10, -100) # Move down
            else:
                 annot.xytext = (10, 10) # Default up-right

            annot.set_visible(True)
            fig.canvas.draw_idle()

    fig.canvas.mpl_connect("motion_notify_event", hover)
    
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
