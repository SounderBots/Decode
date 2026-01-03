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

