import json
import numpy as np
import pandas as pd
import plotly.graph_objects as go
from plotly.subplots import make_subplots


def find_ramp_start(df_slice: pd.DataFrame, power_col: str, max_p: float) -> float:
    """Locate when power began ramping up before a shot event."""
    peak_idx = df_slice[power_col][::-1].idxmax()
    slice_indices = df_slice.index.tolist()
    try:
        loc = slice_indices.index(peak_idx)
    except ValueError:
        loc = len(slice_indices) - 1

    ramp_start_time = df_slice.loc[peak_idx, "Timestamp"]
    slope_threshold = 0.02

    for i in range(loc, 0, -1):
        curr_idx = slice_indices[i]
        prev_idx = slice_indices[i - 1]
        curr_val = df_slice.loc[curr_idx, power_col]
        prev_val = df_slice.loc[prev_idx, power_col]
        diff = curr_val - prev_val

        if diff < -0.01:
            ramp_start_time = df_slice.loc[curr_idx, "Timestamp"]
            break
        if diff < slope_threshold and curr_val < (max_p - 0.1):
            ramp_start_time = df_slice.loc[curr_idx, "Timestamp"]
            break

        ramp_start_time = df_slice.loc[prev_idx, "Timestamp"]

    return ramp_start_time


def detect_changes_in_window(
    df: pd.DataFrame,
    start_time: float,
    end_time: float,
    tps_cols: list,
    drop_threshold_pct: float = 0.10,
    target_col: str | None = None,
    power_map: dict | None = None,
) -> list:
    """Detect shot times by correlating velocity drops with optional power ramps.

    Only relies on provided column names; callers must pass explicit `tps_cols` and, if
    available, a target column and per-velocity power column mapping. When no power
    column is supplied for a velocity series, the drop crossing time is used instead of
    a ramp-derived time so detection still works on minimal logs.
    """
    try:
        if not tps_cols:
            return []

        start_idx = df["Timestamp"].searchsorted(start_time)
        end_idx = df["Timestamp"].searchsorted(end_time)

        if start_idx >= len(df) or start_idx == end_idx:
            return []

        window_df = df.iloc[start_idx:end_idx].copy()
        if window_df.empty:
            return []

        if not target_col or target_col not in df.columns:
            return []

        target_tps = df.iloc[start_idx][target_col]

        detected_shots = []
        current_pos = 0

        while current_pos < len(window_df):
            remaining_df = window_df.iloc[current_pos:]
            threshold_val = target_tps * (1.0 - drop_threshold_pct)

            drop_mask = pd.Series(False, index=remaining_df.index)
            for col in tps_cols:
                drop_mask |= remaining_df[col] < threshold_val

            if not drop_mask.any():
                break

            rel_drop_pos = drop_mask.values.argmax()
            drop_pos_in_window = current_pos + rel_drop_pos
            drop_cross_time = window_df.iloc[drop_pos_in_window]["Timestamp"]

            event_detected_times = []
            for col in tps_cols:
                power_col = None
                if power_map and isinstance(power_map, dict):
                    power_col = power_map.get(col)

                if power_col and power_col in df.columns:
                    pre_drop_slice = df[
                        (df["Timestamp"] <= drop_cross_time)
                        & (df["Timestamp"] >= drop_cross_time - 0.5)
                    ]
                    if not pre_drop_slice.empty:
                        min_p = pre_drop_slice[power_col].min()
                        max_p = pre_drop_slice[power_col].max()
                        if max_p - min_p > 0.4:
                            ramp_start_time = find_ramp_start(pre_drop_slice, power_col, max_p)
                            event_detected_times.append(ramp_start_time)
                        else:
                            min_indices = pre_drop_slice[pre_drop_slice[power_col] <= min_p + 0.001].index
                            if not min_indices.empty:
                                fallback_time = pre_drop_slice.loc[min_indices[-1], "Timestamp"]
                                event_detected_times.append(fallback_time)
                else:
                    # Without a mapped power column, use the drop crossing time as a fallback.
                    event_detected_times.append(drop_cross_time)

            if event_detected_times:
                shot_time = max(event_detected_times)

                if detected_shots and (shot_time - detected_shots[-1] < 0.1):
                    current_pos = drop_pos_in_window + 5
                    continue

                detected_shots.append(shot_time)

                next_search_time = shot_time + 0.2
                next_pos = window_df["Timestamp"].searchsorted(next_search_time)
                current_pos = max(next_pos, drop_pos_in_window + 5)
            else:
                current_pos = drop_pos_in_window + 5

        return detected_shots

    except Exception as exc:  # pragma: no cover - guardrail
        print(f"Warning: Error detecting shot times: {exc}")
        return []


def _padded_bounds(y_min: float, y_max: float, pad_frac: float = 0.05) -> tuple:
    """Return padded (lo, hi) bounds with optional non-negative floor when data are non-negative."""
    if y_min is None or y_max is None or not np.isfinite(y_min) or not np.isfinite(y_max):
        return 0.0, 1.0
    span = y_max - y_min
    if span <= 0:
        span = max(abs(y_max), 1.0)
    pad = pad_frac * span
    lower = y_min - pad
    upper = y_max + pad
    if y_min >= 0 and lower < 0:
        lower = 0
    return lower, upper


def _y_bounds_for_velocity(df: pd.DataFrame, cols: list) -> tuple:
    cols = [c for c in cols if c in df.columns]
    if not cols:
        return 0.0, 1.0

    vel_values = df[cols].to_numpy(copy=False)
    y_min = np.nanmin(vel_values)
    y_max = np.nanmax(vel_values)

    # Ensure reasonable vertical headroom so traces sit near the middle and cap to 1000 if data is lower
    y_min = min(y_min, 0)
    y_max = max(y_max, 1000)

    return _padded_bounds(y_min, y_max)


def _add_vertical_spans(fig, x_vals, y_min, y_max, name, color, width, dash, opacity):
    x_seq = []
    y_seq = []
    for t in x_vals:
        x_seq.extend([t, t, None])
        y_seq.extend([y_min, y_max, None])
    fig.add_trace(
        go.Scatter(
            x=x_seq,
            y=y_seq,
            mode="lines",
            line=dict(color=color, dash=dash, width=width),
            opacity=opacity,
            name=name,
        ),
        row=1,
        col=1,
    )


def _add_unstable_bands(fig, times, y_min, y_max):
    """Add unstable bands and return their trace indices for bulk toggle."""
    band_traced = False
    indices = []
    for t in times:
        x_band = [t - 0.05, t + 0.05, t + 0.05, t - 0.05, t - 0.05, None]
        y_band = [y_min, y_min, y_max, y_max, y_min, None]
        fig.add_trace(
            go.Scatter(
                x=x_band,
                y=y_band,
                mode="lines",
                line=dict(width=0),
                fill="toself",
                fillcolor="rgba(255,165,0,0.5)",
                opacity=0.5,
                name="Premature/Unstable" if not band_traced else None,
                showlegend=not band_traced,
            ),
            row=1,
            col=1,
        )
        indices.append(len(fig.data) - 1)
        band_traced = True
    return indices


def create_analysis_plot(
    df: pd.DataFrame,
    actual_shot_times: list,
    unstable_shot_times: list,
    column_map: dict | None = None,
    power_map: dict | None = None,
) -> dict:
    """Create Plotly JSON for velocity/power analysis using explicit column mappings.

    Dependencies:
    - Timestamp column is required ("Timestamp").
    - Velocity series come from `column_map['velocity']`.
    - Target series come from `column_map['target_for_velocity']` or `column_map['target_default']`.
    - Optional command/ready columns may be provided in the mapping.
    - Optional `power_map` provides per-velocity power columns.
    """

    column_map = column_map or {}
    power_map = power_map or {}

    tps_cols = [c for c in column_map.get("velocity", []) if c in df.columns]
    target_for_velocity = column_map.get("target_for_velocity", {}) or {}
    default_target = column_map.get("target_default")
    command_col = column_map.get("command") or column_map.get("shoot")
    ready_col = column_map.get("ready")

    target_cols = set()
    for val in target_for_velocity.values():
        if val in df.columns:
            target_cols.add(val)
    if default_target in df.columns:
        target_cols.add(default_target)

    has_power = any((power_map.get(col) in df.columns) for col in tps_cols)

    rows = 2 if has_power else 1
    fig = make_subplots(
        rows=rows,
        cols=1,
        shared_xaxes=True,
        vertical_spacing=0.1,
        subplot_titles=("Motor Velocity Tracking", "Motor Power") if has_power else ("Motor Velocity Tracking",),
    )

    # Legend controls for show/hide all data layers (kept out of data_trace_indices)
    fig.add_trace(
        go.Scatter(
            x=[0, 1],
            y=[0, 0],
            mode="lines",
            line=dict(color="#1f78ff", width=3),
            name="Show all",
            legendgroup="datalayers",
            legendgrouptitle_text="Data layers",
            hoverinfo="skip",
        )
    )
    fig.add_trace(
        go.Scatter(
            x=[0, 1],
            y=[0, 0],
            mode="lines",
            line=dict(color="#9ca3af", width=3),
            name="Hide all",
            legendgroup="datalayers",
            legendgrouptitle_text="Data layers",
            hoverinfo="skip",
        )
    )

    timestamps_array = df["Timestamp"].to_numpy(copy=False)
    timestamps = timestamps_array.tolist()
    data_trace_indices = []
    first_velocity_group = True
    first_power_group = True

    y_min_plot, y_max_plot = _y_bounds_for_velocity(df, list(target_cols) + tps_cols)

    for tgt in sorted(target_cols):
        fig.add_trace(
            go.Scatter(
                x=timestamps,
                y=df[tgt].tolist(),
                name=tgt,
                legendgroup="velocity",
                legendgrouptitle_text="Velocity" if first_velocity_group else None,
                line=dict(color="#2ca02c", dash="dash", width=2),
                mode="lines",
            ),
            row=1,
            col=1,
        )
        data_trace_indices.append(len(fig.data) - 1)
        first_velocity_group = False

    if command_col and command_col in df.columns:
        shooting_times = df[df[command_col] == 1]["Timestamp"].tolist()
        if shooting_times:
            _add_vertical_spans(
                fig,
                shooting_times,
                y_min_plot,
                y_max_plot,
                name=command_col,
                color="purple",
                width=1,
                dash="dash",
                opacity=0.4,
            )
            data_trace_indices.append(len(fig.data) - 1)

    if ready_col and ready_col in df.columns:
        ready_times = df[df[ready_col] == 1]["Timestamp"].tolist()
        if ready_times:
            _add_vertical_spans(
                fig,
                ready_times,
                y_min_plot,
                y_max_plot,
                name=ready_col,
                color="green",
                width=1,
                dash="solid",
                opacity=0.3,
            )
            # Start hidden by default; also respect show/hide all controls
            fig.update_traces(visible="legendonly", selector=dict(name="Ready Signal"))

    if actual_shot_times:
        _add_vertical_spans(
            fig,
            actual_shot_times,
            y_min_plot,
            y_max_plot,
            name="Actual Shot",
            color="red",
            width=2,
            dash="solid",
            opacity=0.6,
        )
        data_trace_indices.append(len(fig.data) - 1)

    if unstable_shot_times:
        unstable_indices = _add_unstable_bands(fig, unstable_shot_times, y_min_plot, y_max_plot)
        data_trace_indices.extend(unstable_indices)

    palette = ["#1f77b4", "#e3c800", "#9467bd", "#d62728", "#17becf"]
    color_map = {}
    for i, col in enumerate(tps_cols):
        color_map[col] = palette[i % len(palette)]

    motor_traces = []
    for tps_col in tps_cols:
        color = color_map[tps_col]
        fig.add_trace(
            go.Scatter(
                x=timestamps,
                y=df[tps_col].tolist(),
                name=tps_col,
                legendgroup="velocity",
                legendgrouptitle_text="Velocity" if first_velocity_group else None,
                line=dict(color=color, width=1.5),
                mode="lines",
            ),
            row=1,
            col=1,
        )
        data_trace_indices.append(len(fig.data) - 1)
        motor_traces.append((tps_col, df[tps_col]))
        first_velocity_group = False

    if len(motor_traces) == 2:
        col1_name, col1_data = motor_traces[0]
        col2_name, col2_data = motor_traces[1]

        shooting_phase_mask = pd.Series(False, index=df.index)
        if actual_shot_times:
            for t in actual_shot_times:
                window_mask = (df["Timestamp"] >= t) & (df["Timestamp"] <= t + 0.5)
                window_df = df.loc[window_mask]
                if not window_df.empty:
                    t_min1 = window_df.loc[window_df[col1_name].idxmin()]["Timestamp"]
                    t_min2 = window_df.loc[window_df[col2_name].idxmin()]["Timestamp"]
                    end_time = max(t_min1, t_min2)
                    shooting_phase_mask |= (df["Timestamp"] >= t) & (df["Timestamp"] <= end_time)
        else:
            shooting_phase_mask = pd.Series(False, index=df.index)

        mask_list = shooting_phase_mask.tolist()
        y_lower = np.minimum(col1_data.values, col2_data.values).tolist()
        y_upper = np.maximum(col1_data.values, col2_data.values).tolist()

        segments = []
        start = None
        for i, m in enumerate(mask_list):
            if m and start is None:
                start = i
            elif not m and start is not None:
                segments.append((start, i - 1))
                start = None
        if start is not None:
            segments.append((start, len(mask_list) - 1))

        for idx, (s, e) in enumerate(segments):
            x_seg = timestamps[s : e + 1]
            lower_seg = y_lower[s : e + 1]
            upper_seg = y_upper[s : e + 1]

            fig.add_trace(
                go.Scatter(
                    x=x_seg,
                    y=lower_seg,
                    mode="lines",
                    line=dict(width=0),
                    showlegend=False,
                    hoverinfo="skip",
                ),
                row=1,
                col=1,
            )
            fig.add_trace(
                go.Scatter(
                    x=x_seg,
                    y=upper_seg,
                    mode="lines",
                    line=dict(width=0),
                    fill="tonexty",
                    fillcolor="rgba(255, 0, 0, 0.3)",
                    name="L/R Sync Mismatch" if idx == 0 else None,
                    showlegend=(idx == 0),
                ),
                row=1,
                col=1,
            )
            data_trace_indices.append(len(fig.data) - 1)
            data_trace_indices.append(len(fig.data) - 2)

    if has_power:
        for tps_col in tps_cols:
            color = color_map[tps_col]
            power_col = power_map.get(tps_col)
            if power_col and power_col in df.columns:
                fig.add_trace(
                    go.Scatter(
                        x=timestamps,
                        y=df[power_col].tolist(),
                        name=power_col,
                        legendgroup="power",
                        legendgrouptitle_text="Motor Power" if first_power_group else None,
                        line=dict(color=color, width=1.5),
                        mode="lines",
                    ),
                    row=2,
                    col=1,
                )
                data_trace_indices.append(len(fig.data) - 1)
                first_power_group = False

        fig.add_hline(y=1, line_dash="dot", line_color="red", opacity=0.5, row=2, col=1)
        fig.add_hline(y=-1, line_dash="dot", line_color="red", opacity=0.5, row=2, col=1)
        fig.add_hline(y=0, line_color="black", opacity=0.3, row=2, col=1)

    fig.update_layout(
        height=800,
        hovermode="x unified",
        legend=dict(
            orientation="v",
            y=1,
            x=1.02,
            xanchor="left",
            yanchor="top",
            tracegroupgap=12,
            traceorder="grouped",
            font=dict(color="#1f2933"),
        ),
        margin=dict(t=20),
        paper_bgcolor="#f8f9fa",
        plot_bgcolor="#f8f9fa",
        meta=dict(data_trace_indices=data_trace_indices),
    )

    t_min = float(np.nanmin(timestamps_array)) if timestamps_array.size else 0.0
    t_max = float(np.nanmax(timestamps_array)) if timestamps_array.size else 0.0

    fig.update_yaxes(title_text="Velocity", row=1, col=1)
    if has_power:
        fig.update_yaxes(title_text="Power", row=2, col=1)

    # Single shared x-axis update instead of repeating per row
    fig.update_xaxes(title_text="Time (s)", range=[t_min, t_max], autorange=False, row="all", col=1)

    return json.loads(fig.to_json())


def _valid_column(df: pd.DataFrame, col: str) -> bool:
    return isinstance(col, str) and col in df.columns


def _collect_event_times(df: pd.DataFrame, event_col: str) -> list:
    try:
        series = df[event_col]
        if series.dtype == object:
            mask = series.fillna("").astype(str).str.len() > 0
        else:
            mask = series.fillna(0).astype(float) != 0
        return df.loc[mask, "Timestamp"].tolist()
    except Exception:
        return []


def create_custom_plot(df: pd.DataFrame, customization: dict) -> dict | None:
    """Build custom subplots from user customization profile."""
    if not customization:
        return None

    subplots = customization.get("subplots") or []
    if not subplots:
        return None

    declared_cols = customization.get("columns")
    if declared_cols:
        df_cols = list(df.columns)[: len(declared_cols)]
        if df_cols != declared_cols:
            return None

    devices = customization.get("devices") or []
    events = customization.get("events") or []
    devices_by_name = {d.get("name"): d for d in devices if d.get("name")}
    event_by_key = {}
    for evt in events:
        if not isinstance(evt, dict):
            continue
        col = evt.get("column")
        label = evt.get("label") or col
        key = label or col
        if key:
            event_by_key[key] = evt

    fig = make_subplots(
        rows=len(subplots),
        cols=1,
        shared_xaxes=True,
        vertical_spacing=0.08,
        subplot_titles=[sp.get("title") or f"Subplot {i+1}" for i, sp in enumerate(subplots)],
    )

    palette = ["#1f77b4", "#e3c800", "#9467bd", "#d62728", "#17becf", "#2ca02c", "#ff7f0e"]
    event_default_color = "#e74c3c"
    legend_keys = []

    for i, sp in enumerate(subplots):
        row = i + 1
        legend_id = "legend" if i == 0 else f"legend{i+1}"
        legend_keys.append((legend_id, row))
        entries = sp.get("columns") or []
        legend_overrides = sp.get("legendOverrides") or []
        override_map = {}
        order_map = {}
        for idx, item in enumerate(legend_overrides):
            if not isinstance(item, dict):
                continue
            key = item.get("key")
            label = item.get("label")
            if not key:
                continue
            normalized_key = key.replace(".", "::") if "::" not in key and "." in key else key
            override_map[normalized_key] = label
            order_map[normalized_key] = idx
        extra_rank = len(order_map)

        def legend_rank(key: str | None) -> int:
            nonlocal extra_rank
            if key and key in order_map:
                return order_map[key]
            extra_rank += 1
            return extra_rank
        traces_for_diff = []
        data_mins = []
        data_maxs = []

        target_counts = {}
        for entry in entries:
            if entry in devices_by_name:
                t_col = devices_by_name[entry].get("target")
                if t_col:
                    target_counts[t_col] = target_counts.get(t_col, 0) + 1
        rendered_targets = set()

        def track(col_name: str):
            try:
                data_mins.append(df[col_name].min())
                data_maxs.append(df[col_name].max())
            except Exception:
                pass

        for j, entry in enumerate(entries):
            if _valid_column(df, entry):
                color = palette[(j + i) % len(palette)]
                fig.add_trace(
                    go.Scatter(
                        x=df["Timestamp"].tolist(),
                        y=df[entry].tolist(),
                        name=override_map.get(entry, entry),
                        mode="lines",
                        line=dict(color=color, width=1.6),
                        legend=legend_id,
                        legendrank=legend_rank(entry),
                    ),
                    row=row,
                    col=1,
                )
                traces_for_diff.append((entry, df[entry]))
                track(entry)
            elif entry in devices_by_name:
                device = devices_by_name[entry]
                measured = device.get("measured")
                target = device.get("target")
                command = device.get("command")
                color = palette[(j + i) % len(palette)]
                base_label = override_map.get(entry, entry)

                if _valid_column(df, measured):
                    fig.add_trace(
                        go.Scatter(
                            x=df["Timestamp"].tolist(),
                            y=df[measured].tolist(),
                            name=override_map.get(f"{entry}::measured", f"{base_label} measured"),
                            mode="lines",
                            line=dict(color=color, width=1.8),
                            legend=legend_id,
                            legendrank=legend_rank(f"{entry}::measured"),
                        ),
                        row=row,
                        col=1,
                    )
                    traces_for_diff.append((f"{entry}::measured", df[measured]))
                    track(measured)

                if _valid_column(df, target):
                    target_seen = target in rendered_targets
                    target_shared = target_counts.get(target, 0) > 1
                    if target_shared and target_seen:
                        pass  # already added a shared target trace
                    else:
                        target_key = f"target::{target}" if target_shared else f"{entry}::target"
                        label = override_map.get(target_key)
                        if not label:
                            label = "target" if target_shared else f"{base_label} target"
                        fig.add_trace(
                            go.Scatter(
                                x=df["Timestamp"].tolist(),
                                y=df[target].tolist(),
                                name=label,
                                mode="lines",
                                line=dict(color=color, dash="dash", width=1.2),
                                legend=legend_id,
                                legendrank=legend_rank(target_key),
                            ),
                            row=row,
                            col=1,
                        )
                        track(target)
                        if target_shared:
                            rendered_targets.add(target)

                if _valid_column(df, command):
                    fig.add_trace(
                        go.Scatter(
                            x=df["Timestamp"].tolist(),
                            y=df[command].tolist(),
                            name=override_map.get(f"{entry}::command", f"{base_label} command"),
                            mode="lines",
                            line=dict(color=color, dash="dot", width=1.2),
                            opacity=0.7,
                            legend=legend_id,
                            legendrank=legend_rank(f"{entry}::command"),
                        ),
                        row=row,
                        col=1,
                    )
                    track(command)
                    traces_for_diff.append((f"{entry}::command", df[command]))

        if sp.get("diff") and len(traces_for_diff) >= 2:
            diff_keys = sp.get("diffKeys") or []
            available_keys = {k: series for k, series in traces_for_diff}
            key_list = list(available_keys.keys())
            a_key = diff_keys[0] if len(diff_keys) > 0 and diff_keys[0] in available_keys else (key_list[0] if key_list else None)
            b_key = diff_keys[1] if len(diff_keys) > 1 and diff_keys[1] in available_keys else (key_list[1] if len(key_list) > 1 else None)
            if a_key and b_key:
                try:
                    a_series = available_keys[a_key]
                    b_series = available_keys[b_key]
                    a_vals = a_series.to_numpy(copy=False)
                    b_vals = b_series.to_numpy(copy=False)
                    ts = df["Timestamp"].to_numpy(copy=False)
                    m = min(len(a_vals), len(b_vals), len(ts))
                    if m > 1:
                        x_vals = ts[:m]
                        lower = np.minimum(a_vals[:m], b_vals[:m])
                        upper = np.maximum(a_vals[:m], b_vals[:m])

                        fig.add_trace(
                            go.Scatter(
                                x=x_vals,
                                y=lower,
                                mode="lines",
                                line=dict(width=0),
                                showlegend=False,
                                hoverinfo="skip",
                                legend=legend_id,
                                legendrank=legend_rank(None),
                            ),
                            row=row,
                            col=1,
                        )
                        fig.add_trace(
                            go.Scatter(
                                x=x_vals,
                                y=upper,
                                mode="lines",
                                line=dict(width=0),
                                fill="tonexty",
                                fillcolor="rgba(255, 0, 0, 0.3)",
                                name="Diff",
                                showlegend=True,
                                legend=legend_id,
                                legendrank=legend_rank(None),
                            ),
                            row=row,
                            col=1,
                        )
                except Exception:
                    pass

        if data_mins and data_maxs:
            try:
                ymin = float(np.nanmin(data_mins))
                ymax = float(np.nanmax(data_maxs))
                y_lo, y_hi = _padded_bounds(ymin, ymax)
            except Exception:
                y_lo, y_hi = (0, 1)
        else:
            y_lo, y_hi = (0, 1)

        allowed_event_keys = sp.get("events") or []
        if events and allowed_event_keys:
            for evt in events:
                col = evt.get("column")
                label = evt.get("label") or col
                key = label or col
                if not key:
                    continue
                if key not in allowed_event_keys:
                    continue
                color = evt.get("color") or event_default_color
                style = evt.get("style") or "dash"
                if style not in {"solid", "dash", "dot", "dashdot", "longdash", "longdashdot"}:
                    style = "dash"
                if _valid_column(df, col):
                    times = _collect_event_times(df, col)
                    if times:
                        x_vals = []
                        y_vals = []
                        for t in times:
                            x_vals.extend([t, t, None])
                            y_vals.extend([y_lo, y_hi, None])
                        fig.add_trace(
                            go.Scatter(
                                x=x_vals,
                                y=y_vals,
                                mode="lines",
                                line=dict(color=color, width=1.2, dash=style),
                                opacity=0.5,
                                name=label,
                                legend=legend_id,
                                legendrank=legend_rank(None),
                            ),
                            row=row,
                            col=1,
                        )

        if data_mins and data_maxs:
            fig.update_yaxes(range=[y_lo, y_hi], autorange=False, row=row, col=1)

    legend_layouts = {}
    for legend_id, row in legend_keys:
        yaxis_key = "yaxis" if row == 1 else f"yaxis{row}"
        domain = fig.layout[yaxis_key].domain if yaxis_key in fig.layout else [0.0, 1.0]
        y_center = float(sum(domain) / 2.0) if domain else 0.5
        legend_layouts[legend_id] = dict(
            orientation="v",
            y=y_center,
            yanchor="middle",
            x=1.02,
            xanchor="left",
            traceorder="grouped",
            tracegroupgap=6,
            bgcolor="rgba(248, 249, 250, 0.85)",
            bordercolor="#d1d5db",
            borderwidth=1,
        )

    fig.update_layout(
        height=max(320, 280 * len(subplots)),
        hovermode="x unified",
        margin=dict(t=40, b=40, r=160, l=60),
        **legend_layouts,
    )
    fig.update_xaxes(title_text="Time (s)", row="all", col=1)

    return json.loads(fig.to_json())
