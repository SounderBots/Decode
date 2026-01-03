# Analysis Server Architecture (Internal)

## Overview
The Analysis Server is a FastAPI app that ingests FTC robot log CSVs, builds Plotly-based visualizations, and serves an interactive report page. Client and server share a single source of truth for Times of Interest (TOI) detection: the server detects change points; the client renders tables and derives band diffs for display.

## High-Level Flow
1) Upload: Client posts CSV + customization JSON to `/analyze`.
2) Server parse: `parser` loads CSV; customization is parsed; session cached.
3) Analysis: column mappings resolved; loop stats optional; TOI payload starts empty.
4) Visualization: `visualizer` builds Plotly figure; fallback to default plot if custom fails.
5) Render: `report.html` receives plot, metadata, and session id.
6) TOI Apply: Client posts settings to `/updateToiSettings`; server returns change times per subplot; client computes band diffs from traces and renders tables.

## Key Modules
- `app/main.py`: FastAPI app, static/templating, request logging, global exception handler.
- `app/routers/analyze.py`: Upload handling, session cache, TOI update endpoint, logging.
- `app/core/parser.py`: CSV parsing and basic validation.
- `app/core/analyzer.py`: Column mapping, loop stats, TOI change detection.
- `app/core/visualizer.py`: Plotly figure construction for uploaded data and fallback presets.
- `app/templates/report.html`: Client UI (Plotly render, TOI controls, tables) and JS TOI apply logic.
- `app/static/style.css`: Styling, including TOI row classes.

## TOI Design
- Source of truth: server change detection (`detect_toi_changes`) per subplot.
- Apply endpoint: `/updateToiSettings` accepts `{subplotIndex, column, direction, thresholdMode, thresholdValue, minSpacingMs, maxRows}`; returns `{subplots:[{index, rows:[{time}]}], warnings}`.
- Initial state: `toi_payload` is empty; TOI tables populate only after Apply.
- Client behavior: auto-apply once per interest subplot on load; renders rows; computes start/max band diffs from traces via nearest-point lookup (binary search on monotonic traces, linear fallback).

## Logging & Error Handling
- Request middleware logs method/path/status/duration; global exception handler returns JSON 500 and logs stack traces.
- Analyze route logs customization parse, plot build/fallback, upload accepted.
- `/updateToiSettings` logs session id, subplot count, warnings, and row counts; detection failures log exceptions with subplot context.

## Performance Notes
- TOI nearest lookup optimized with binary search when traces are monotonic; linear fallback otherwise.
- Latency/stability computations removed from request path (TOI flow is server-driven only).
- CSV size capped (3MB) to bound memory footprint.

## Session Cache
- `session_cache` stores dataframe, customization, and column map keyed by session id for use by `/updateToiSettings`.

## Extensibility
- To add new detectors: implement in `analyzer`, wire endpoint to include additional fields, and update client render logic.
- To add new plots: extend `visualizer` and template binding; ensure fallbacks stay light.

