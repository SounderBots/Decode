import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from sklearn.linear_model import LinearRegression
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
        return df
    except pd.errors.EmptyDataError:
        print(f"Error: File {file_path} is empty.")
        return None
    except Exception as e:
        print(f"Error parsing CSV: {e}")
        return None

def derive_coefficients(file_path, save_plot=False):
    print(f"Deriving coefficients from {file_path}...")
    df = parse_log_file(file_path)
    if df is None:
        return

    if 'Timestamp' not in df.columns:
        print("Error: Missing 'Timestamp' column.")
        return

    # Find motor columns (ending in TPS, excluding TargetTPS)
    tps_cols = [c for c in df.columns if c.endswith('TPS') and c != 'TargetTPS']
    
    if not tps_cols:
        print("Error: No actual TPS columns found.")
        print(f"Available columns: {df.columns.tolist()}")
        return

    print(f"Found motor columns: {tps_cols}")

    # Setup combined figure
    num_motors = len(tps_cols)
    plt.figure(figsize=(10, 5 * num_motors))

    for i, tps_col in enumerate(tps_cols):
        prefix = tps_col[:-3]
        power_col = f"{prefix}Power"
        
        # Fallback logic for legacy/single motor logs
        if prefix == "Actual":
             if "TotalPower" in df.columns: power_col = "TotalPower"
        elif prefix == "":
             if "TotalPower" in df.columns: power_col = "TotalPower"
        
        if power_col not in df.columns:
            # Try TotalPower as last resort if only 1 motor
            if len(tps_cols) == 1 and "TotalPower" in df.columns:
                power_col = "TotalPower"
            else:
                print(f"Skipping {tps_col}: Could not find power column '{power_col}'")
                continue

        print(f"\n--- Analyzing Motor: {tps_col} (Power: {power_col}) ---")

        # --- Preprocessing ---
        # Calculate Acceleration
        # Accel = d(Velocity) / d(Time)
        df['dt'] = df['Timestamp'].diff()
        df['dVel'] = df[tps_col].diff()
        
        # Avoid division by zero or noise from duplicate timestamps
        with np.errstate(divide='ignore', invalid='ignore'):
            df['Accel'] = df['dVel'] / df['dt']

        # Filter Data
        # 1. Remove low velocity (static friction noise)
        # 2. Remove saturated power (where the motor can't physically comply)
        # 3. Remove infinite acceleration (div by zero) or NaNs
        
        # Thresholds
        min_vel = 10.0 # TPS
        max_power = 0.95
        
        mask = (df[tps_col].abs() > min_vel) & \
               (df[power_col].abs() < max_power) & \
               (np.isfinite(df['Accel'])) & \
               (df['dt'] > 0.0001) # Ensure positive time step
               
        df_clean = df[mask].copy()
        
        if len(df_clean) < 10:
            print(f"Error: Not enough valid data points after filtering for {tps_col}.")
            continue

        # --- Linear Regression ---
        # Model: Voltage = kS * sgn(Vel) + kV * Vel + kA * Accel
        # We are solving for kS, kV, kA
        
        # Features (X)
        X = pd.DataFrame({
            'SignVel': np.sign(df_clean[tps_col]), # For kS
            'Vel': df_clean[tps_col],               # For kV
            'Accel': df_clean['Accel']              # For kA
        })
        
        # Target (y)
        y = df_clean[power_col]
        
        # Fit model without intercept (because kS is handled by SignVel)
        model = LinearRegression(fit_intercept=False)
        model.fit(X, y)
        
        kS = model.coef_[0]
        kV = model.coef_[1]
        kA = model.coef_[2]
        
        r_squared = model.score(X, y)

        print("-" * 30)
        print(f"Derived Coefficients for {tps_col}:")
        print(f"kS (Static Friction): {kS:.5f}")
        print(f"kV (Velocity Constant): {kV:.7f}")
        print(f"kA (Acceleration Constant): {kA:.7f}")
        print(f"R² (Fit Quality): {r_squared:.4f}")
        print("-" * 30)
        
        # --- Validation Plot (Subplot) ---
        # Predict Power using derived coefficients
        df_clean['PredictedPower'] = model.predict(X)
        
        plt.subplot(num_motors, 1, i + 1)
        plt.scatter(df_clean['Timestamp'], df_clean[power_col], label='Actual Power', alpha=0.5, s=10)
        plt.plot(df_clean['Timestamp'], df_clean['PredictedPower'], label=f'Predicted (R²={r_squared:.2f})', color='red', alpha=0.7)
        plt.title(f'System Identification Fit: {tps_col}')
        plt.ylabel('Motor Power')
        plt.legend()
        plt.grid(True)
    
    plt.xlabel('Time (s)')
    plt.tight_layout()
    
    if save_plot:
        img_path = file_path.replace('.csv', '_fit.png')
        plt.savefig(img_path)
        print(f"Combined plot saved to {img_path}")
        
        # Open automatically
        try:
            if os.name == 'nt':
                os.startfile(img_path)
            elif sys.platform == 'darwin':
                os.system(f'open "{img_path}"')
            else:
                os.system(f'xdg-open "{img_path}"')
        except Exception as e:
            print(f"Warning: Could not open plot automatically: {e}")
    else:
        sys.stdout.flush()
        plt.show()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Derive Motor Feedforward Coefficients (kS, kV, kA)')
    parser.add_argument('file', nargs='?', help='Path to the CSV log file')
    parser.add_argument('--save', action='store_true', help='Save plot to PNG instead of showing it')
    args = parser.parse_args()

    if args.file:
        derive_coefficients(args.file, args.save)
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
            derive_coefficients(latest_file, args.save)
        else:
            print("No CSV log files found.")
