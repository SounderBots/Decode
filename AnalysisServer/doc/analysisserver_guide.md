# Analysis Server User Guide

## What it does
Upload a robot log CSV and get an interactive Plotly report with charts and Times of Interest (TOI) tables that highlight notable changes in your signals.

## Prepare your CSV
- Include a timestamp column (default name `Timestamp`).
- Keep file size under ~3 MB (server limit).
- Column names become series in the plots; consistent naming helps mapping devices.

## Run an analysis
1) Open the Analysis page and upload your CSV.
2) (Optional) Provide customization JSON to predefine subplots/series/labels.
3) Submit; the server parses the file and renders the report with Plotly charts.

## Times of Interest (TOI)
- TOI detection runs on the server when you click **Apply** in the TOI panel.
- Settings per subplot: series, direction (`rising|falling|either`), threshold mode (`absolute|percent`), threshold value, min spacing (ms), max rows (cap at 50).
- The server returns change timestamps; the client renders the table and computes start/max band diffs from the plotted series for display.
- Hover/click rows to focus the chart at that time.

## Customizing
- Use the TOI controls in the left panel; adjust threshold/direction/min spacing, then click **Apply**.
- Save profile to persist your customization locally (client-side).
- Subplots and series come from your CSV columns; legend overrides in customization JSON can rename series for display.

## Tips for clean data
- Ensure timestamps are numeric and increasing; noisy or non-monotonic timestamps reduce TOI accuracy.
- Avoid mixed-type columns; keep numeric signals numeric.
- Trim empty rows and header quirks before upload.

## Troubleshooting
- "Session missing": reload and re-upload the CSV.
- No TOI rows returned: check threshold/direction and ensure the chosen column exists.
- TOI values look off: verify your series and timestamp columns are correctly selected and monotonic.

## Privacy & limits
- No authentication; keep uploads within the size cap.
- Server logs requests and basic stats for debugging (no data persisted beyond session cache).

