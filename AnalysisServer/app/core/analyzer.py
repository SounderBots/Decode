import math

import pandas as pd
import numpy as np
from app.core import visualizer


def _append_unique(seq: list, value):
    if value is None:
        return
    if value not in seq:
        seq.append(value)


def resolve_column_mappings(df: pd.DataFrame, customization: dict | None = None) -> dict:
    """Return column mappings derived from the dataframe and optional customization.

    Only uses columns present in the parsed dataframe (timestamp is required). Relies on
    user-provided customization for semantic mapping; no implicit legacy fallbacks. All
    non-timestamp columns are optional and analysis gracefully no-ops if absent.
    """

    columns = list(df.columns)
    df_cols = set(columns)

    # Timestamp mapping: only use explicit customization or existing column match
    ts_custom = customization.get("timestamp") if isinstance(customization, dict) else None
    ts_col = ts_custom if ts_custom in df_cols else ("Timestamp" if "Timestamp" in df_cols else None)

    velocity_cols: list[str] = []  # measured series
    target_candidates: list[str] = []  # potential target series
    target_by_velocity: dict[str, str] = {}

    command_col = None

    if isinstance(customization, dict):
        # Explicit measured/target lists
        for measured in customization.get("measured", []) or []:
            if measured in df_cols:
                _append_unique(velocity_cols, measured)
        for target in customization.get("target", []) or []:
            if target in df_cols:
                _append_unique(target_candidates, target)

        # Devices declare measured/target pairs
        devices = customization.get("devices") or []
        for device in devices:
            if not isinstance(device, dict):
                continue
            measured = device.get("measured")
            target = device.get("target")

            if measured in df_cols:
                _append_unique(velocity_cols, measured)
            if target in df_cols:
                _append_unique(target_candidates, target)
                if measured in df_cols:
                    target_by_velocity[measured] = target

        # Explicit command column
        if customization.get("command") in df_cols:
            command_col = customization.get("command")

        # Events can carry the command column
        if command_col is None:
            events = customization.get("events") or []
            for evt in events:
                if not isinstance(evt, dict):
                    continue
                col = evt.get("column")
                if col in df_cols:
                    command_col = col
                    break

    default_target = target_candidates[0] if target_candidates else None
    target_for_velocity = {col: target_by_velocity.get(col, default_target) for col in velocity_cols}

    return {
        "timestamp": ts_col,
        "command": command_col,
        "shoot": command_col,  # legacy alias kept for downstream compatibility
        "velocity": velocity_cols,
        "target_default": default_target,
        "target_for_velocity": target_for_velocity,
    }

def calculate_loop_stats(df: pd.DataFrame, column_map: dict | None = None):
    """Calculates loop frequency statistics using mapped timestamp column when provided."""
    stats = {}
    columns = column_map or resolve_column_mappings(df)
    ts_col = columns.get('timestamp') or 'Timestamp'
    if ts_col not in df.columns:
        return stats

    dt_full = df[ts_col].diff().fillna(0.01)
    dt = dt_full[1:]
    
    if not dt.empty:
        dt_ms = dt * 1000
        stats['median_ms'] = float(dt_ms.median())
        stats['hz'] = float(1000 / stats['median_ms']) if stats['median_ms'] > 0 else 0
        stats['min_ms'] = float(dt_ms.min())
        stats['max_ms'] = float(dt_ms.max())
        stats['mean_ms'] = float(dt_ms.mean())
        
        top_spikes = dt_ms.nlargest(3)
        stats['spikes'] = []
        for idx, val in top_spikes.items():
            timestamp = df.loc[idx, ts_col]
            stats['spikes'].append({'val': float(val), 'time': float(timestamp)})
            
    return stats

def calculate_shot_latency(df: pd.DataFrame, column_map: dict | None = None):
    """Calculates shot latency statistics using mapped column names when provided."""
    stats = {'latencies': [], 'shots': []}

    columns = column_map or resolve_column_mappings(df)
    ts_col = columns.get('timestamp') or 'Timestamp'
    tps_cols = columns.get('velocity') or []
    shoot_col = columns.get('command') or columns.get('shoot')
    target_for_velocity = columns.get('target_for_velocity') or {}

    if not tps_cols or shoot_col not in df.columns or ts_col not in df.columns:
        return stats

    is_shooting = df[shoot_col].fillna(0)
    if is_shooting.sum() <= 0:
        return stats

    rising_edges = (is_shooting.diff() == 1) | ((is_shooting == 1) & is_shooting.shift(fill_value=0).eq(0))
    command_indices = df.index[rising_edges]
    command_times = df.loc[command_indices, ts_col].values

    actual_shot_times = []
    latencies = []

    primary_tps = tps_cols[0]
    target_col = target_for_velocity.get(primary_tps) or columns.get('target_default')

    for i, t_cmd in enumerate(command_times):
        if i < len(command_times) - 1:
            t_next = command_times[i + 1]
            window_end = min(t_cmd + 3.0, t_next)
        else:
            window_end = t_cmd + 3.0

        changes_in_window = visualizer.detect_changes_in_window(df, t_cmd, window_end, tps_cols, target_col=target_col)

        if changes_in_window:
            t_first = changes_in_window[0]
            latency = t_first - t_cmd
            # Ignore any negative latency caused by early detection noise; clamp to zero.
            if latency < 0:
                latency = 0.0
            latencies.append(latency)
            actual_shot_times.extend(changes_in_window)

    stats['shots'] = actual_shot_times

    if latencies:
        latencies_ms = [l * 1000 for l in latencies]
        stats['median_ms'] = float(np.median(latencies_ms))
        stats['min_ms'] = float(min(latencies_ms))
        stats['max_ms'] = float(max(latencies_ms))
        stats['mean_ms'] = float(np.mean(latencies_ms))

    return stats

def calculate_readiness_stability(df: pd.DataFrame, actual_shot_times: list, column_map: dict | None = None):
    """Calculates readiness and stability metrics for each shot using mapped column names."""
    results = []

    columns = column_map or resolve_column_mappings(df)
    ts_col = columns.get('timestamp') or 'Timestamp'
    tps_cols = columns.get('velocity') or []
    target_for_velocity = columns.get('target_for_velocity') or {}
    default_target = columns.get('target_default')

    if not tps_cols or ts_col not in df.columns:
        return results, []

    # Readiness
    TOLERANCE_PCT = 0.05
    ready_mask = pd.Series(True, index=df.index)
    active_mask = pd.Series(False, index=df.index)

    usable_velocity_cols = []

    for tps_col in tps_cols:
        target_col = target_for_velocity.get(tps_col) or default_target
        if target_col not in df.columns:
            continue
        usable_velocity_cols.append((tps_col, target_col))

        target_series = df[target_col]
        tolerance_abs = (target_series.abs() * TOLERANCE_PCT).clip(lower=50)
        error_series = target_series - df[tps_col]
        in_range = error_series.notna() & tolerance_abs.notna() & (error_series.abs() <= tolerance_abs)
        ready_mask = ready_mask & in_range
        active_mask = active_mask | (target_series.abs() > 100)

    active_ready_mask = ready_mask & active_mask

    # Stability
    dt_full = df[ts_col].diff().fillna(0.01)
    unstable_mask = pd.Series(False, index=df.index)

    if not usable_velocity_cols:
        return results, []

    for tps_col, target_col in usable_velocity_cols:

        velocity = df[tps_col]
        acceleration = velocity.diff() / dt_full
        accel_threshold = df[target_col].abs() * 2.0
        accel_threshold = accel_threshold.clip(lower=500)
        is_unstable = acceleration.notna() & accel_threshold.notna() & (acceleration.abs() > accel_threshold)
        unstable_mask = unstable_mask | is_unstable

    unstable_shot_times = []

    for i, t_shot in enumerate(actual_shot_times):
        idx = df[ts_col].searchsorted(t_shot)
        if idx >= len(df):
            idx = len(df) - 1

        is_ready = bool(active_ready_mask.iloc[idx])
        is_unstable = bool(unstable_mask.iloc[idx])

        shot_info = {
            'index': i + 1,
            'time': float(t_shot),
            'ready': is_ready,
            'stable': not is_unstable,
            'status': []
        }

        if not is_ready:
            shot_info['status'].append("NOT READY")
        if is_unstable:
            shot_info['status'].append("UNSTABLE")

        # Disparity
        if len(tps_cols) == 2:
            col1 = tps_cols[0]
            col2 = tps_cols[1]
            val1 = df[col1].iloc[idx]
            val2 = df[col2].iloc[idx]
            shot_info['start_diff'] = float(abs(val1 - val2))

            window_mask = (df[ts_col] >= t_shot) & (df[ts_col] <= t_shot + 0.5)
            window_df = df.loc[window_mask]
            max_drop_diff = 0.0
            if not window_df.empty:
                t_min1 = window_df.loc[window_df[col1].idxmin()][ts_col]
                t_min2 = window_df.loc[window_df[col2].idxmin()][ts_col]
                end_time = max(t_min1, t_min2)
                drop_slice = window_df[window_df[ts_col] <= end_time]
                if not drop_slice.empty:
                    diffs = (drop_slice[col1] - drop_slice[col2]).abs()
                    max_drop_diff = float(diffs.max())
            shot_info['max_drop_diff'] = max_drop_diff

        results.append(shot_info)

        if shot_info['status']:
            unstable_shot_times.append(t_shot)

    return results, unstable_shot_times


def detect_toi_changes(
    df: pd.DataFrame,
    column: str,
    ts_col: str = "Timestamp",
    direction: str = "rising",
    threshold_mode: str = "percent",
    threshold_value: float = 5.0,
    min_spacing_ms: float = 500,
    max_rows: int = 50,
) -> list[dict]:
    """Detect change points mirroring client TOI logic (delta-based, capped rows).

    direction: 'rising' | 'falling' | 'either'
    threshold_mode: 'percent' | 'absolute'
    threshold_value: numeric value used per mode
    min_spacing_ms: minimum spacing between detections, in milliseconds
    max_rows: cap on returned rows
    """

    if column not in df.columns or ts_col not in df.columns:
        return []

    series = df[column]
    times = df[ts_col]

    if series.empty or times.empty:
        return []

    y = series.to_numpy()
    x = times.to_numpy()

    # Compute max absolute value for percent mode; fallback to 0 if not finite.
    with np.errstate(all="ignore"):
        max_abs = np.nanmax(np.abs(y))
    if not np.isfinite(max_abs):
        max_abs = 0.0

    delta_threshold = threshold_value
    if threshold_mode == "percent":
        delta_threshold = max_abs * (threshold_value / 100.0)

    if not np.isfinite(delta_threshold):
        return []

    spacing_sec = (min_spacing_ms or 0) / 1000.0
    rows = []
    last_time = -math.inf

    n = min(len(x), len(y))
    for i in range(1, n):
        t_curr = x[i]
        t_prev = x[i - 1]
        v_curr = y[i]
        v_prev = y[i - 1]

        # Ensure numeric values
        if not (np.isfinite(t_curr) and np.isfinite(t_prev) and np.isfinite(v_curr) and np.isfinite(v_prev)):
            continue

        delta = v_curr - v_prev
        delta_mag = abs(delta)

        crossed = False
        if direction == "rising" or direction == "either":
            if delta > 0 and delta_mag >= delta_threshold:
                crossed = True
        if not crossed and (direction == "falling" or direction == "either"):
            if delta < 0 and delta_mag >= delta_threshold:
                crossed = True

        if not crossed:
            continue

        if (t_curr - last_time) < spacing_sec:
            continue

        rows.append({"time": float(t_curr)})
        last_time = t_curr
        if len(rows) >= (max_rows or 50):
            break

    return rows

