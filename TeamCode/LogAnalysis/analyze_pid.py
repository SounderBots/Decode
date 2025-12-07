import pandas as pd
import matplotlib.pyplot as plt
import argparse
import os
import sys
import io

# ANSI Colors
BLUE = '\033[94m'
RED = '\033[91m'
RESET = '\033[0m'

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

def detect_shot_times_in_window(df, start_time, end_time, tps_cols, drop_threshold_pct=0.10):
    """
    Detects ALL actual shot times within a given window by looking for significant TPS drops
    and correlating them with Power ramp-ups.
    Returns a list of unique shot times.
    """
    try:
        # Define search window
        start_idx = df['Timestamp'].searchsorted(start_time)
        end_idx = df['Timestamp'].searchsorted(end_time)
        
        if start_idx >= len(df) or start_idx == end_idx:
            return []
            
        window_df = df.iloc[start_idx:end_idx].copy()
        if window_df.empty: return []
        
        # Get target velocity (use mean of window or start)
        target_tps = df.iloc[start_idx]['TargetTPS']
        if abs(target_tps) < 10: return []
        
        detected_shots = []
        
        # We need to iterate through the window to find multiple shots.
        # Strategy: Find a drop, identify the shot time, then advance the window past the recovery.
        
        current_pos = 0
        
        while current_pos < len(window_df):
            # Look for the next drop starting from current_pos
            remaining_df = window_df.iloc[current_pos:]
            
            # Find where ANY motor drops below threshold
            threshold_val = target_tps * (1.0 - drop_threshold_pct)
            
            # Create a mask for drops in any motor
            drop_mask = pd.Series(False, index=remaining_df.index)
            for col in tps_cols:
                drop_mask |= (remaining_df[col] < threshold_val)
                
            if not drop_mask.any():
                break # No more drops
                
            # Get the first timestamp where a drop occurs
            # drop_mask is a Series with the same index as remaining_df
            # We want the position relative to remaining_df
            rel_drop_pos = drop_mask.values.argmax()
            drop_pos_in_window = current_pos + rel_drop_pos
            
            drop_cross_time = window_df.iloc[drop_pos_in_window]['Timestamp']
            
            # Now analyze this specific event to find the exact start (Power Ramp)
            # We look backwards from the drop_cross_time
            
            event_detected_times = []
            
            for col in tps_cols:
                prefix = col[:-3]
                power_col = f"{prefix}Power"
                
                if power_col in df.columns:
                    # Look in the window [drop_cross_time - 0.5s, drop_cross_time]
                    # We use the main df to look backwards safely
                    pre_drop_slice = df[(df['Timestamp'] <= drop_cross_time) & (df['Timestamp'] >= drop_cross_time - 0.5)]
                    
                    if not pre_drop_slice.empty:
                        min_p = pre_drop_slice[power_col].min()
                        max_p = pre_drop_slice[power_col].max()
                        
                        if max_p - min_p > 0.4:
                            # Backwards Slope Logic
                            # Find the LAST peak (closest to the drop) to avoid finding the previous shot's peak
                            peak_idx = pre_drop_slice[power_col][::-1].idxmax()
                            slice_indices = pre_drop_slice.index.tolist()
                            try:
                                loc = slice_indices.index(peak_idx)
                            except ValueError:
                                loc = len(slice_indices) - 1
                                
                            ramp_start_time = pre_drop_slice.loc[peak_idx]['Timestamp']
                            slope_threshold = 0.02
                            
                            for i in range(loc, 0, -1):
                                curr_idx = slice_indices[i]
                                prev_idx = slice_indices[i-1]
                                curr_val = pre_drop_slice.loc[curr_idx, power_col]
                                prev_val = pre_drop_slice.loc[prev_idx, power_col]
                                diff = curr_val - prev_val
                                
                                if diff < -0.01: 
                                    ramp_start_time = pre_drop_slice.loc[curr_idx]['Timestamp']
                                    break
                                if diff < slope_threshold:
                                    # Only stop if we are NOT on the peak plateau
                                    if curr_val < (max_p - 0.1):
                                        ramp_start_time = pre_drop_slice.loc[curr_idx]['Timestamp']
                                        break
                                    # Else continue walking back across the plateau
                                
                                ramp_start_time = pre_drop_slice.loc[prev_idx]['Timestamp']
                            
                            event_detected_times.append(ramp_start_time)
                        else:
                            # Fallback: Last minimum
                            min_indices = pre_drop_slice[pre_drop_slice[power_col] <= min_p + 0.001].index
                            if not min_indices.empty:
                                fallback_time = pre_drop_slice.loc[min_indices[-1]]['Timestamp']
                                event_detected_times.append(fallback_time)
            
            if event_detected_times:
                # Use the latest time (max) to be robust against noise
                shot_time = max(event_detected_times)
                
                # Debounce: Ignore shots too close to the previous one
                if detected_shots and (shot_time - detected_shots[-1] < 0.1):
                    # This is likely noise or the same shot detected again
                    # Just advance past the current drop to avoid infinite loops
                    current_pos = drop_pos_in_window + 5 # Skip a few frames
                    continue

                detected_shots.append(shot_time)
                
                # Advance window: Skip 0.2s to avoid re-detecting the same shot/noise
                # Find index corresponding to shot_time + 0.2s
                next_search_time = shot_time + 0.2
                
                # Convert next_search_time to an integer location in window_df
                next_pos = window_df['Timestamp'].searchsorted(next_search_time)
                
                # Ensure we always advance at least past the current drop
                current_pos = max(next_pos, drop_pos_in_window + 5)
                
            else:
                # If we found a drop but couldn't pinpoint a start time, skip past it anyway
                current_pos = drop_pos_in_window + 5 # Skip a few frames
                
        return detected_shots
        
    except Exception as e:
        print(f"Warning: Error detecting shot times: {e}")
        return []

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

    # --- Loop Frequency Analysis ---
    # Calculate time differences between consecutive rows
    dt = df['Timestamp'].diff().dropna()
    
    if not dt.empty:
        # Convert to milliseconds for easier reading
        dt_ms = dt * 1000
        
        # Calculate stats
        freq_min = dt_ms.min()
        freq_max = dt_ms.max()
        freq_median = dt_ms.median()
        freq_mean = dt_ms.mean()
        
        print(f"\n{BLUE}--- Loop Frequency Analysis ---{RESET}")
        print(f"Median Loop Time: {freq_median:.2f} ms ({(1000/freq_median):.1f} Hz)")
        print(f"Min Loop Time:    {freq_min:.2f} ms")
        print(f"Max Loop Time:    {freq_max:.2f} ms")
        print(f"Mean Loop Time:   {freq_mean:.2f} ms")
        
        # Find top 3 spikes
        top_spikes = dt_ms.nlargest(3)
        print("Top 3 spikes:")
        for idx, val in top_spikes.items():
            timestamp = df.loc[idx, 'Timestamp']
            print(f"  - {val:.2f} ms at {timestamp:.3f}s")
        
        # Optional: Warn if loop time is very high (e.g., > 50ms)
        if freq_max > 50:
            print("WARNING: High loop time spikes detected (>50ms). This may affect PID performance.")

    # --- Shot Latency Analysis ---
    actual_shot_times = []
    if 'IsShooting' in df.columns and df['IsShooting'].sum() > 0:
        print(f"\n{BLUE}--- Shot Latency Analysis ---{RESET}")
        print("Latency is measured as the time difference between the 'IsShooting' command and the start of the motor power ramp-up.")
        
        # Find rising edges of IsShooting
        is_shooting = df['IsShooting'].values
        # Prepend 0 to detect rising edge at index 0
        is_shooting_padded = pd.concat([pd.Series([0]), df['IsShooting']], ignore_index=True).values
        rising_edges = (is_shooting_padded[1:] == 1) & (is_shooting_padded[:-1] == 0)
        command_indices = df.index[rising_edges]
        command_times = df.loc[command_indices, 'Timestamp'].values
        
        latencies = []
        
        for i, t_cmd in enumerate(command_times):
            # Determine window end: Next command or +3 seconds
            if i < len(command_times) - 1:
                t_next = command_times[i+1]
                window_end = min(t_cmd + 3.0, t_next)
            else:
                window_end = t_cmd + 3.0
                
            shots_in_window = detect_shot_times_in_window(df, t_cmd, window_end, tps_cols)
            
            if shots_in_window:
                # First shot latency
                t_first = shots_in_window[0]
                latency = t_first - t_cmd
                latencies.append(latency)
                
                actual_shot_times.extend(shots_in_window)
        
        if latencies:
            latencies_ms = [l * 1000 for l in latencies]
            lat_min = min(latencies_ms)
            lat_max = max(latencies_ms)
            lat_mean = sum(latencies_ms) / len(latencies_ms)
            lat_median = sorted(latencies_ms)[len(latencies_ms) // 2]

            print(f"Median Latency: {lat_median:.0f} ms")
            print(f"Min Latency:    {lat_min:.0f} ms")
            print(f"Max Latency:    {lat_max:.0f} ms")
            print(f"Mean Latency:   {lat_mean:.0f} ms")

    df_full = df.copy() # Save full data for metrics

    # Check if ANY motor has power columns to decide on 3rd subplot
    # We do a quick check for likely power columns
    has_power = False
    for tps_col in tps_cols:
        prefix = tps_col[:-3]
        if f"{prefix}Power" in df.columns or "TotalPower" in df.columns:
            has_power = True
            break
            
    num_plots = 2 if has_power else 1

    # --- Readiness Analysis (Calculation) ---
    # Define Tolerance (User can adjust this in code or via args later, default 5%)
    TOLERANCE_PCT = 0.05
    
    # Calculate "Ready" state: Both motors within tolerance
    ready_mask = pd.Series(True, index=df.index)
    
    for tps_col in tps_cols:
        prefix = tps_col[:-3]
        error_col = f"{prefix}Error"
        if error_col not in df.columns:
             df[error_col] = df['TargetTPS'] - df[tps_col]
        
        target_abs = df['TargetTPS'].abs()
        tolerance_abs = target_abs * TOLERANCE_PCT
        tolerance_abs = tolerance_abs.clip(lower=50) 
        
        in_range = df[error_col].abs() <= tolerance_abs
        ready_mask = ready_mask & in_range

    # Calculate Active Ready Mask
    active_ready_mask = ready_mask & (df['TargetTPS'].abs() > 100)

    # --- Stability Analysis (Calculation & Printing) ---
    dt = df['Timestamp'].diff().fillna(0.01)
    unstable_mask = pd.Series(False, index=df.index)
    
    for tps_col in tps_cols:
        velocity = df[tps_col]
        acceleration = velocity.diff() / dt
        accel_threshold = df['TargetTPS'].abs() * 2.0 
        accel_threshold = accel_threshold.clip(lower=500)
        is_unstable = acceleration.abs() > accel_threshold
        unstable_mask = unstable_mask | is_unstable

    unstable_shot_times = [] # Store for plotting later

    if actual_shot_times:
        print(f"\n{BLUE}--- Shot Stability Analysis ---{RESET}")
        print("Velocity differences are measured in Ticks Per Second (TPS).")
        for i, t_shot in enumerate(actual_shot_times):
            idx = df['Timestamp'].searchsorted(t_shot)
            if idx >= len(df): idx = len(df) - 1
            
            is_ready = active_ready_mask.iloc[idx]
            is_unstable = unstable_mask.iloc[idx]
            
            status = []
            if not is_ready: status.append("NOT READY")
            if is_unstable: status.append("UNSTABLE")
            
            status_str = f": {', '.join(status)}" if status else ": OK (Ready & Stable)"
            
            disparity_info = ""
            start_diff = 0.0
            if len(tps_cols) == 2:
                col1 = tps_cols[0]
                col2 = tps_cols[1]
                val1 = df[col1].iloc[idx]
                val2 = df[col2].iloc[idx]
                start_diff = abs(val1 - val2)
                
                window_mask = (df['Timestamp'] >= t_shot) & (df['Timestamp'] <= t_shot + 0.5)
                window_df = df.loc[window_mask]
                max_drop_diff = 0.0
                if not window_df.empty:
                    t_min1 = window_df.loc[window_df[col1].idxmin()]['Timestamp']
                    t_min2 = window_df.loc[window_df[col2].idxmin()]['Timestamp']
                    end_time = max(t_min1, t_min2)
                    drop_slice = window_df[window_df['Timestamp'] <= end_time]
                    if not drop_slice.empty:
                        diffs = (drop_slice[col1] - drop_slice[col2]).abs()
                        max_drop_diff = diffs.max()
                disparity_info = f" | Start Diff: {start_diff:.1f} | Max Drop Diff: {max_drop_diff:.1f}"

            idx_str = f"{i+1}."
            # Color the index red if start_diff > 0
            idx_display = f"{RED}{idx_str:<4}{RESET}" if start_diff > 0 else f"{idx_str:<4}"
            print(f"  {idx_display} Shot at {t_shot:.3f}s{disparity_info}")
            
            if status:
                unstable_shot_times.append(t_shot)

    # --- Plotting Data Selection ---
    plot_configs = [] # List of dicts: {'df': df, 'shots': [], 'title': ''}
    
    if actual_shot_times:
        total_duration = df['Timestamp'].iloc[-1] - df['Timestamp'].iloc[0]
        num_shots = len(actual_shot_times)
        
        if num_shots > 3 or total_duration >= 60:
            print(f"\nLog contains {num_shots} shots over {total_duration:.1f}s.")
            print("Enter shot indexes to plot (e.g. '1, 3-5, 10') or 'all' for continuous plot:")
            try:
                user_input = input("Selection: ").strip()
                if not user_input:
                    sys.exit(0)

                if user_input.lower() == 'all':
                     plot_configs.append({'df': df, 'shots': actual_shot_times, 'title': 'Full Log'})
                else:
                    selected_indices = set()
                    parts = user_input.split(',')
                    for part in parts:
                        part = part.strip()
                        if '-' in part:
                            start, end = map(int, part.split('-'))
                            selected_indices.update(range(start, end + 1))
                        else:
                            selected_indices.add(int(part))
                    
                    # Filter valid indices
                    valid_indices = sorted([i for i in selected_indices if 0 < i <= num_shots])
                    
                    if len(valid_indices) > 3:
                        print(f"{RED}Selection limited to first 3 shots.{RESET}")
                        valid_indices = valid_indices[:3]
                    
                    for i in valid_indices:
                        t_shot = actual_shot_times[i-1]
                        # Create window for this shot: -1s to +1.5s
                        mask = (df['Timestamp'] >= t_shot - 1.0) & (df['Timestamp'] <= t_shot + 1.5)
                        shot_df = df[mask].copy()
                        if not shot_df.empty:
                            plot_configs.append({
                                'df': shot_df, 
                                'shots': [t_shot], 
                                'title': f'Shot {i}'
                            })

            except Exception as e:
                print(f"Error parsing selection: {e}. Plotting all shots.")
                plot_configs.append({'df': df, 'shots': actual_shot_times, 'title': 'Full Log'})
        else:
             plot_configs.append({'df': df, 'shots': actual_shot_times, 'title': 'Full Log'})
    else:
        # No shots detected, plot everything
        plot_configs.append({'df': df, 'shots': [], 'title': 'Full Log'})

    # --- Plotting Loop ---
    # Define Color Map (User Preference: Green=Target, Blue=Left, Yellow=Right)
    color_map = {}
    palette = ['#1f77b4', '#e3c800', '#9467bd', '#d62728', '#17becf']
    left_col = next((c for c in tps_cols if 'Left' in c), None)
    right_col = next((c for c in tps_cols if 'Right' in c), None)
    if left_col and right_col and len(tps_cols) == 2:
        color_map[left_col] = '#1f77b4'
        color_map[right_col] = '#e3c800'
    else:
        for i, col in enumerate(tps_cols):
            color_map[col] = palette[i % len(palette)]

    for config in plot_configs:
        plot_df = config['df']
        shots_to_plot = config['shots']
        title_suffix = config['title']
        
        # Create Figure
        fig, axes = plt.subplots(num_plots, 1, figsize=(12, 10), sharex=True)
        if num_plots == 1: axes = [axes]
        
        # 1. Velocity (Target) - Plot once
        plt.sca(axes[0])
        plt.plot(plot_df['Timestamp'], plot_df['TargetTPS'], label='Target TPS', color='#2ca02c', linestyle='--', linewidth=2, alpha=0.9)

        # --- New Columns Visualization (ShooterReady & IsShooting) ---
        all_tps_values = pd.concat([plot_df['TargetTPS']] + [plot_df[c] for c in tps_cols])
        y_min_plot = all_tps_values.min()
        y_max_plot = all_tps_values.max()
        if y_max_plot == y_min_plot:
            y_max_plot += 100
            y_min_plot -= 100

        if 'ShooterReady' in plot_df.columns:
            ready_times = plot_df[plot_df['ShooterReady'] == 1]['Timestamp']
            if not ready_times.empty:
                plt.vlines(x=ready_times, ymin=y_min_plot, ymax=y_max_plot, 
                           colors='green', alpha=0.1, label='Ready Signal')

        if 'IsShooting' in plot_df.columns:
            shooting_times = plot_df[plot_df['IsShooting'] == 1]['Timestamp']
            if not shooting_times.empty:
                plt.vlines(x=shooting_times, ymin=y_min_plot, ymax=y_max_plot, 
                           colors='purple', linestyles='--', linewidth=1.5, alpha=0.5, label='Shoot Cmd')
                
        if shots_to_plot:
            plt.vlines(x=shots_to_plot, ymin=y_min_plot, ymax=y_max_plot, 
                       colors='red', linestyles='-', linewidth=2, alpha=0.6, label='Actual Shot')
            
            # Plot Unstable Zones (Filtered)
            first_unstable_plotted = False
            for t_shot in unstable_shot_times:
                if t_shot in shots_to_plot:
                    label = 'Premature/Unstable' if not first_unstable_plotted else None
                    plt.axvspan(t_shot - 0.05, t_shot + 0.05, color='orange', alpha=0.5, label=label)
                    first_unstable_plotted = True



        motor_plot_data = [] # Store data for discrepancy fill

        for i, tps_col in enumerate(tps_cols):
            # Determine prefix (e.g., "Right" from "RightTPS", or "" from "ActualTPS")
            prefix = tps_col[:-3] 
            color = color_map.get(tps_col, palette[i % len(palette)])
            
            # Determine associated columns
            error_col = f"{prefix}Error"
            power_col = f"{prefix}Power"
            
            # Fallback for single motor legacy naming
            if prefix == "Actual":
                if "TotalPower" in plot_df.columns: power_col = "TotalPower"
                if "Error" in plot_df.columns: error_col = "Error"
            elif prefix == "": # Just "TPS" ? Unlikely but possible
                 if "TotalPower" in plot_df.columns: power_col = "TotalPower"

            # Calculate Error if missing
            if error_col not in plot_df.columns:
                plot_df[error_col] = plot_df['TargetTPS'] - plot_df[tps_col]
            
            # --- Add to Plots ---
            
            # 1. Velocity
            plt.sca(axes[0])
            plt.plot(plot_df['Timestamp'], plot_df[tps_col], label=f'{tps_col}', color=color, linewidth=1.5, alpha=0.9)
            motor_plot_data.append((tps_col, plot_df[tps_col]))
            
            # 2. Power (if available)
            if has_power:
                plt.sca(axes[1])
                # Check if specific power col exists, else try TotalPower if it's the only one
                if power_col in plot_df.columns:
                    plt.plot(plot_df['Timestamp'], plot_df[power_col], label=f'{prefix}Power', color=color, alpha=0.8)
                elif "TotalPower" in plot_df.columns and len(tps_cols) == 1:
                     plt.plot(plot_df['Timestamp'], plot_df["TotalPower"], label='TotalPower', color=color, alpha=0.8)

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
            # AND Filter for Shooting Phase (TPS Dropping)
            # Calculate derivative of TPS (sum of both motors for robustness)
            tps_sum = col1_data + col2_data
            tps_diff = tps_sum.diff().fillna(0)
            
            # Define Shooting Phase: Target is high AND TPS is dropping significantly
            # We use a threshold of -10 TPS/step to identify the drop
            # shooting_phase_mask = (df['TargetTPS'].abs() > 100) & (tps_diff < -10)
            
            # Update: Only use ACTUAL detected shot times to define the shooting phase
            shooting_phase_mask = pd.Series(False, index=plot_df.index)
            if shots_to_plot:
                for t in shots_to_plot:
                    # Define a window for the "shooting path"
                    # From the detected start time (ramp) till the min velocity that either of the motors dropped to
                    
                    # Search window: Shot start to +0.5s (to find the minimum)
                    window_mask = (plot_df['Timestamp'] >= t) & (plot_df['Timestamp'] <= t + 0.5)
                    window_df = plot_df.loc[window_mask]
                    
                    if not window_df.empty:
                        # Find time of min velocity for both motors
                        t_min1 = window_df.loc[window_df[col1_name].idxmin()]['Timestamp']
                        t_min2 = window_df.loc[window_df[col2_name].idxmin()]['Timestamp']
                        
                        # End time is the later of the two minimums (covering the full drop phase)
                        end_time = max(t_min1, t_min2)
                        
                        # Update mask
                        shooting_phase_mask |= (plot_df['Timestamp'] >= t) & (plot_df['Timestamp'] <= end_time)
            else:
                # Fallback if no shots detected: Use the drop logic
                shooting_phase_mask = (plot_df['TargetTPS'].abs() > 100) & (tps_diff < -10)
            
            plt.sca(axes[0])
            # Only fill discrepancy during shooting phase
            plt.fill_between(plot_df['Timestamp'], col1_data, col2_data, 
                             where=shooting_phase_mask,
                             color='red', alpha=0.3, label='L/R Sync Mismatch')

        plt.sca(axes[0])
        plt.title(f'Velocity Tracking: {os.path.basename(file_path)} - {title_suffix}\nOrange Zone = Unstable Shot | Red Zone = Sync Mismatch')
        plt.ylabel('Velocity (TPS)')
        plt.legend(loc='lower right')
        plt.grid(True, which='both', linestyle='--', alpha=0.7)

        if has_power:
            plt.sca(axes[1])
            plt.ylabel('Power (-1 to 1)')
            plt.title('Motor Power')
            plt.xlabel('Time (s)')
            plt.axhline(y=1, color='red', linestyle=':', alpha=0.5)
            plt.axhline(y=-1, color='red', linestyle=':', alpha=0.5)
            plt.axhline(y=0, color='black', linestyle='-', alpha=0.3)
            
            plt.legend(loc='lower right')
            plt.grid(True, which='both', linestyle='--', alpha=0.7)
        else:
            plt.sca(axes[0])
            plt.xlabel('Time (s)')
                
        plt.tight_layout()

        # --- Interactive Cursor (Optimized) ---
        # Use a vertical line that snaps to data points to avoid constant redraws
        
        # Use plot_df for cursor alignment
        vlines = [ax.axvline(x=plot_df['Timestamp'].iloc[0], color='gray', linestyle='--', alpha=0.8, visible=False) for ax in axes]
        
        # Annotation box on the first subplot
        annot = axes[0].annotate("", xy=(0,0), xytext=(10,10), textcoords="offset points",
                                bbox=dict(boxstyle="round", fc="w", alpha=0.9),
                                arrowprops=dict(arrowstyle="->"), zorder=10)
        annot.set_visible(False)
        
        # Store last index to prevent unnecessary redraws
        last_idx = [None]

        def update_annot(idx, _plot_df=plot_df): # Capture plot_df in closure
            x = _plot_df['Timestamp'].iloc[idx]
            
            # Build text with info from all motors
            text_lines = [f"Time: {x:.3f}s", f"Target: {_plot_df['TargetTPS'].iloc[idx]:.0f}"]
            
            for tps_col in tps_cols:
                val = _plot_df[tps_col].iloc[idx]
                text_lines.append(f"{tps_col}: {val:.0f}")
                
                # Add Error info
                prefix = tps_col[:-3]
                err_col = f"{prefix}Error"
                if err_col in _plot_df.columns:
                    text_lines.append(f"{prefix}Err: {_plot_df[err_col].iloc[idx]:.0f}")
                    
            annot.xy = (x, _plot_df['TargetTPS'].iloc[idx])
            annot.set_text("\n".join(text_lines))

        def hover(event, _plot_df=plot_df, _vlines=vlines, _last_idx=last_idx):
            # Performance: Only show if Left Ctrl is pressed AND mouse is inside axes
            should_show = (event.key == 'control') and (event.inaxes in axes)

            if not should_show:
                if annot.get_visible():
                    annot.set_visible(False)
                    for vl in _vlines: vl.set_visible(False)
                    fig.canvas.draw_idle()
                return

            # Find nearest timestamp index
            # Binary search for performance O(log N)
            target_x = event.xdata
            timestamps = _plot_df['Timestamp'].values
            idx = timestamps.searchsorted(target_x)
            
            # Clamp index and find closest
            if idx >= len(timestamps): idx = len(timestamps) - 1
            if idx > 0:
                # Check which is closer: idx or idx-1
                if abs(timestamps[idx] - target_x) > abs(timestamps[idx-1] - target_x):
                    idx = idx - 1
            
            # Only redraw if index changed (Major Performance Optimization)
            if idx != _last_idx[0]:
                _last_idx[0] = idx
                
                # Update position
                x_pos = timestamps[idx]
                for vl in _vlines:
                    vl.set_xdata([x_pos, x_pos])
                    vl.set_visible(True)
                
                update_annot(idx, _plot_df)
                
                # Smart positioning to avoid overlap with title/top
                # Check if we are in the top 20% of the visible y-axis
                ylim = axes[0].get_ylim()
                y_range = ylim[1] - ylim[0]
                y_val = _plot_df['TargetTPS'].iloc[idx]
                
                # If y_val is high up, move tooltip down-right instead of up-right
                if (y_val - ylim[0]) / y_range > 0.8:
                     annot.xytext = (10, -100) # Move down
                else:
                     annot.xytext = (10, 10) # Default up-right

                annot.set_visible(True)
                fig.canvas.draw_idle()

        fig.canvas.mpl_connect("motion_notify_event", hover)
        
        if save_plot:
            img_path = file_path.replace('.csv', f'_{title_suffix.replace(" ", "")}.png')
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

    if not save_plot:
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
