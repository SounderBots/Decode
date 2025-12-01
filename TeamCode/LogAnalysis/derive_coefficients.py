import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from sklearn.linear_model import LinearRegression
import argparse
import os
import sys
import io

# --- Configuration ---
COLORS = {
    'Target': 'green',
    'Actual': 'blue',
    'Predicted': 'red',
    'Fit': 'orange'
}

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

def get_insights(kS, kV, kA, r_squared, max_vel, max_power_used, residuals):
    """
    Generates actionable insights and recommended next steps.
    """
    insights = []
    next_steps = []
    
    # 1. Fit Quality
    if r_squared < 0.6:
        insights.append(f"CRITICAL: Poor Model Fit (R²={r_squared:.2f}). Data is too noisy or lacks range.")
        next_steps.append("ACTION: Run a 'Ramp Test' (slowly accelerate 0 -> Max Power over 3-5s).")
    elif r_squared < 0.85:
        insights.append(f"WARNING: Moderate Model Fit (R²={r_squared:.2f}).")
        next_steps.append("ACTION: Ensure smooth acceleration and minimize gear lash.")
    else:
        insights.append(f"SUCCESS: Good Model Fit (R²={r_squared:.2f}). Coefficients are reliable.")
        next_steps.append(f"ACTION: Update your code: kS={kS:.5f}, kV={kV:.7f}, kA={kA:.7f}")

    # 2. Physical Validity (kS)
    if kS < 0:
        insights.append(f"WARNING: Negative Static Friction (kS={kS:.4f}).")
        next_steps.append("ACTION: Check if motor direction is reversed relative to encoder.")
    elif kS > 0.15:
        insights.append(f"NOTE: High Static Friction (kS={kS:.2f}).")
        next_steps.append("ACTION: Check for mechanical binding or friction in the gearbox.")

    # 3. Physical Validity (kA)
    if kA < 0:
        insights.append(f"WARNING: Negative Acceleration Gain (kA={kA:.5f}).")
        next_steps.append("ACTION: Physics violation. Ignore kA. Collect data with cleaner acceleration.")

    # 4. Headroom
    predicted_max_power = kS + (kV * max_vel)
    if predicted_max_power > 1.0:
        insights.append(f"CRITICAL: Motor Saturation. Predicted power at max speed ({max_vel:.0f}) is {predicted_max_power:.2f}.")
        next_steps.append("ACTION: Reduce max velocity or gear ratio. PID will have no headroom.")
    elif predicted_max_power > 0.9:
        insights.append(f"WARNING: Low Headroom. Predicted power is {predicted_max_power:.2f}.")
        next_steps.append("ACTION: PID may struggle to correct errors at top speed.")
    else:
        insights.append(f"SUCCESS: Good Headroom. Predicted power is {predicted_max_power:.2f}.")
        
    # 5. Residual Analysis (Manual Tuning Advice)
    # Residual = Actual - Predicted
    mean_residual = residuals.mean()
    if abs(mean_residual) > 0.05:
        direction = "Increase" if mean_residual > 0 else "Decrease"
        insights.append(f"NOTE: Model consistently {'under' if mean_residual > 0 else 'over'}-estimates power (Avg Err: {mean_residual:.3f}).")
        next_steps.append(f"MANUAL TWEAK: Try {direction}ing kV slightly if you see steady-state error.")

    return insights, next_steps

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
        return

    print(f"Found motor columns: {tps_cols}")

    num_motors = len(tps_cols)
    # Layout: 3 Rows x N Columns (Motors)
    # Row 1: Velocity Profile
    # Row 2: Voltage vs Velocity (Scatter)
    # Row 3: Actual vs Predicted (Time)
    fig, axes = plt.subplots(3, num_motors, figsize=(6 * num_motors, 12), sharex='row')
    
    # Handle single motor case (axes is 1D array)
    if num_motors == 1:
        axes = np.array([[axes[0]], [axes[1]], [axes[2]]])
    else:
        # Transpose so axes[row][col] works
        pass 

    # Store data for cursor
    cursor_data = {} 
    vlines = []
    annot = None

    for i, tps_col in enumerate(tps_cols):
        prefix = tps_col[:-3]
        power_col = f"{prefix}Power"
        
        # Fallback logic
        if prefix == "Actual":
             if "TotalPower" in df.columns: power_col = "TotalPower"
        elif prefix == "":
             if "TotalPower" in df.columns: power_col = "TotalPower"
        
        if power_col not in df.columns:
            if len(tps_cols) == 1 and "TotalPower" in df.columns:
                power_col = "TotalPower"
            else:
                print(f"Skipping {tps_col}: Could not find power column '{power_col}'")
                continue

        print(f"\n--- Analyzing Motor: {tps_col} ---")

        # --- Preprocessing ---
        df['dt'] = df['Timestamp'].diff()
        df['dVel'] = df[tps_col].diff()
        
        with np.errstate(divide='ignore', invalid='ignore'):
            df['Accel'] = df['dVel'] / df['dt']

        # Filter Data for Regression
        min_vel = 10.0 
        max_power = 0.98 # Ignore saturation
        
        mask = (df[tps_col].abs() > min_vel) & \
               (df[power_col].abs() < max_power) & \
               (np.isfinite(df['Accel'])) & \
               (df['dt'] > 0.0001)
               
        df_clean = df[mask].copy()
        
        if len(df_clean) < 10:
            print(f"Error: Not enough valid data points for {tps_col}.")
            continue

        # --- Linear Regression ---
        X = pd.DataFrame({
            'SignVel': np.sign(df_clean[tps_col]),
            'Vel': df_clean[tps_col],
            'Accel': df_clean['Accel']
        })
        y = df_clean[power_col]
        
        model = LinearRegression(fit_intercept=False)
        model.fit(X, y)
        
        kS, kV, kA = model.coef_
        r_squared = model.score(X, y)

        # --- Insights ---
        max_vel = df_clean[tps_col].abs().max()
        
        # Calculate residuals for insight generation
        # We need to predict on the CLEAN data used for regression to check fit quality
        pred_power_clean = model.predict(X)
        residuals = y - pred_power_clean
        
        insights, next_steps = get_insights(kS, kV, kA, r_squared, max_vel, df_clean[power_col].max(), residuals)

        print(f"Derived Coefficients (R²={r_squared:.4f}):")
        print(f"  kS: {kS:.5f}")
        print(f"  kV: {kV:.7f}")
        print(f"  kA: {kA:.7f}")
        print("Insights:")
        for line in insights:
            print(f"  {line}")
        print("Recommended Next Steps:")
        for step in next_steps:
            print(f"  {step}")

        # --- Plotting ---
        
        # Row 1: Velocity Profile
        ax_vel = axes[0][i] if num_motors > 1 else axes[0][0]
        ax_vel.plot(df['Timestamp'], df[tps_col], label='Actual Vel', color=COLORS['Actual'])
        if 'TargetTPS' in df.columns:
            ax_vel.plot(df['Timestamp'], df['TargetTPS'], label='Target Vel', color=COLORS['Target'], linestyle='--')
        
        # Highlight Saturation (Red Background)
        ax_vel.fill_between(df['Timestamp'], df[tps_col].min(), df[tps_col].max(), 
                            where=(df[power_col].abs() > 0.95), 
                            color='red', alpha=0.1, label='Saturation (>95% Pwr)')
        
        ax_vel.set_title(f"{tps_col} Profile")
        ax_vel.set_ylabel("Velocity (TPS)")
        ax_vel.legend()
        ax_vel.grid(True, alpha=0.3)

        # Row 2: Voltage vs Velocity (Scatter)
        ax_scat = axes[1][i] if num_motors > 1 else axes[1][0]
        ax_scat.scatter(df_clean[tps_col], df_clean[power_col], alpha=0.3, s=5, label='Data', color=COLORS['Actual'])
        
        # Plot fit line (assuming Accel=0 for the line)
        vel_range = np.linspace(df_clean[tps_col].min(), df_clean[tps_col].max(), 100)
        power_pred_static = kS * np.sign(vel_range) + kV * vel_range
        ax_scat.plot(vel_range, power_pred_static, color=COLORS['Fit'], linewidth=2, label=f'Fit (kS={kS:.2f}, kV={kV:.5f})')
        
        # Add Physical Limit Lines
        ax_scat.axhline(1.0, color='red', linestyle='--', alpha=0.5, label='Max Power')
        ax_scat.axhline(-1.0, color='red', linestyle='--', alpha=0.5)
        
        # Calculate Max Theoretical Velocity (where Fit crosses 1.0)
        if kV > 0:
            max_theo_vel = (1.0 - kS) / kV
            ax_scat.axvline(max_theo_vel, color='purple', linestyle=':', label=f'Max Vel ({max_theo_vel:.0f})')
        
        ax_scat.set_title(f"Feedforward Fit (R²={r_squared:.2f})")
        ax_scat.set_xlabel("Velocity (TPS)")
        ax_scat.set_ylabel("Power")
        ax_scat.legend()
        ax_scat.grid(True, alpha=0.3)

        # Row 3: Actual vs Predicted (Time)
        ax_fit = axes[2][i] if num_motors > 1 else axes[2][0]
        
        # Predict for WHOLE dataset (not just clean) to see where it fails
        # We need to calculate Accel for the whole dataset first (already done in df['Accel'])
        # Handle NaNs in Accel for prediction
        df_pred = df.copy()
        df_pred['Accel'] = df_pred['Accel'].fillna(0)
        
        pred_power = kS * np.sign(df_pred[tps_col]) + kV * df_pred[tps_col] + kA * df_pred['Accel']
        
        ax_fit.plot(df['Timestamp'], df[power_col], label='Actual Power', color=COLORS['Actual'], alpha=0.6)
        ax_fit.plot(df['Timestamp'], pred_power, label='Predicted', color=COLORS['Predicted'], linestyle='--', alpha=0.8)
        
        # Highlight Good vs Bad Fit Areas
        residuals = df[power_col] - pred_power
        # Green fill for good fit (error < 0.1)
        ax_fit.fill_between(df['Timestamp'], df[power_col], pred_power, 
                            where=(residuals.abs() < 0.1), 
                            color='green', alpha=0.1, label='Good Fit')
        # Red fill for poor fit (error >= 0.1)
        ax_fit.fill_between(df['Timestamp'], df[power_col], pred_power, 
                            where=(residuals.abs() >= 0.1), 
                            color='red', alpha=0.2, label='Poor Fit (>0.1 Err)')

        ax_fit.set_title("Model Validation")
        ax_fit.set_xlabel("Time (s)")
        ax_fit.set_ylabel("Power")
        ax_fit.legend()
        ax_fit.grid(True, alpha=0.3)

        # Add vertical line for cursor
        vl = ax_fit.axvline(x=df['Timestamp'].iloc[0], color='gray', linestyle='--', alpha=0.5)
        vl.set_visible(False)
        vlines.append(vl)
        
        # Also add vline to velocity plot
        vl2 = ax_vel.axvline(x=df['Timestamp'].iloc[0], color='gray', linestyle='--', alpha=0.5)
        vl2.set_visible(False)
        vlines.append(vl2)

        # Store data for cursor
        cursor_data[i] = {
            'df': df,
            'pred_power': pred_power,
            'tps_col': tps_col,
            'power_col': power_col
        }

    plt.tight_layout()

    # --- Interactive Cursor ---
    annot = axes[0][0].annotate("", xy=(0,0), xytext=(20,20), textcoords="offset points",
                    bbox=dict(boxstyle="round", fc="w", alpha=0.9),
                    arrowprops=dict(arrowstyle="->"))
    annot.set_visible(False)

    last_idx = [0]

    def update_annot(idx, motor_idx):
        data = cursor_data[motor_idx]
        df_local = data['df']
        tps_col = data['tps_col']
        power_col = data['power_col']
        
        x = df_local['Timestamp'].iloc[idx]
        
        text_lines = [f"t={x:.3f}s"]
        text_lines.append(f"Vel: {df_local[tps_col].iloc[idx]:.0f}")
        text_lines.append(f"Pwr: {df_local[power_col].iloc[idx]:.2f}")
        text_lines.append(f"Pred: {data['pred_power'].iloc[idx]:.2f}")
        
        annot.xy = (x, df_local[tps_col].iloc[idx])
        annot.set_text("\n".join(text_lines))

    def hover(event):
        # Only show if Left Ctrl is pressed
        if event.key != 'control':
            if annot.get_visible():
                annot.set_visible(False)
                for vl in vlines: vl.set_visible(False)
                fig.canvas.draw_idle()
            return

        # Determine which column (motor) we are in
        found_ax = False
        motor_idx = 0
        
        # Flatten axes list for searching
        all_axes = axes.flatten() if isinstance(axes, np.ndarray) else [axes]
        
        if event.inaxes not in all_axes:
            return

        # Find which column (motor) this axis belongs to
        # axes is [rows][cols]
        if num_motors == 1:
            motor_idx = 0
        else:
            # Find column index
            for c in range(num_motors):
                col_axes = axes[:, c]
                if event.inaxes in col_axes:
                    motor_idx = c
                    break
        
        data = cursor_data[motor_idx]
        df_local = data['df']
        timestamps = df_local['Timestamp'].values
        
        # Binary search
        idx = timestamps.searchsorted(event.xdata)
        if idx >= len(timestamps): idx = len(timestamps) - 1
        
        if idx != last_idx[0]:
            last_idx[0] = idx
            
            # Update all vlines
            x_pos = timestamps[idx]
            for vl in vlines:
                vl.set_xdata([x_pos, x_pos])
                vl.set_visible(True)
            
            # Update annotation
            update_annot(idx, motor_idx)
            annot.set_visible(True)
            
            # Move annotation to the correct axis (the one hovered)
            annot.axes = event.inaxes
            
            # Smart positioning
            ylim = event.inaxes.get_ylim()
            if (event.ydata - ylim[0]) / (ylim[1] - ylim[0]) > 0.8:
                annot.xytext = (10, -100)
            else:
                annot.xytext = (10, 10)

            fig.canvas.draw_idle()

    fig.canvas.mpl_connect("motion_notify_event", hover)

    if save_plot:
        img_path = file_path.replace('.csv', '_fit.png')
        plt.savefig(img_path)
        print(f"Combined plot saved to {img_path}")
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
        print("\nTip: Hold 'Left Ctrl' and hover over the plots to see values.")
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