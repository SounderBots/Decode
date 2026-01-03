import pandas as pd
import io


def parse_log_file(content: bytes):
    """Parse the DataLogger CSV content with validation. Returns (df, metadata_lines, error)."""
    try:
        text_content = content.decode("utf-8")
    except UnicodeDecodeError as err:
        return None, [], f"File is not valid UTF-8 encoded text ({err})."

    lines = text_content.splitlines()
    if not lines:
        return None, [], "File is empty."

    first_line = lines[0].strip()
    if not first_line.startswith("#"):
        return None, [], "First row must be a comment header (start with '#') containing column names."

    header = first_line.lstrip("#").strip()
    if "," not in header:
        return None, [], "Header row must contain comma-separated column names."

    # Collect metadata comment lines until the first data row; ignore other comment lines for data parse
    metadata_lines = []
    body_lines = []
    data_started = False
    for raw in lines[1:]:
        stripped = raw.strip()
        if not data_started and stripped.startswith("#"):
            meta_text = stripped.lstrip("#").strip()
            # Skip metadata lines that are just timestamps
            try:
                first_token = meta_text.split(',')[0].strip()
                if first_token:
                    numeric_val = pd.to_numeric(first_token, errors="coerce")
                    if pd.notna(numeric_val):
                        # Timestamp-like; skip
                        continue
            except Exception:
                pass

            # Remove empty trailing columns
            parts = [p.strip() for p in meta_text.split(',') if p.strip()]
            cleaned_meta = ", ".join(parts) if parts else meta_text
            metadata_lines.append(cleaned_meta)
            continue
        if stripped == "":
            continue
        if stripped.startswith("#"):
            # Comments after data starts are ignored for parsing
            continue
        data_started = True
        body_lines.append(raw)

    try:
        df = pd.read_csv(io.StringIO("\n".join([header] + body_lines)))
    except pd.errors.ParserError:
        return None, metadata_lines, "Could not parse CSV content."
    except Exception:
        return None, metadata_lines, "Unexpected error while parsing CSV."

    # Robust rename: handle "# Timestamp", "#Timestamp", "Timestamp"
    df.rename(columns=lambda x: x.replace("#", "").strip(), inplace=True)

    if df.empty:
        return None, metadata_lines, "No data rows found."

    if "Timestamp" not in df.columns:
        return None, metadata_lines, "Missing required 'Timestamp' column."

    # Validate first column is Timestamp
    first_col = df.columns[0]
    if first_col != "Timestamp":
        return None, metadata_lines, "First column must be 'Timestamp'."

    # Convert columns to numeric
    for col in df.columns:
        df[col] = pd.to_numeric(df[col], errors="coerce")

    if df["Timestamp"].isna().any():
        return None, metadata_lines, "Timestamp column contains invalid values."

    timestamps = df["Timestamp"].to_numpy(copy=False)
    if timestamps.size == 0 or not ((timestamps[1:] > timestamps[:-1]).all()):
        return None, metadata_lines, "Timestamp column must be strictly increasing."

    return df, metadata_lines, None


def validate_log_dataframe(df: pd.DataFrame, customization: dict | None = None):
    """Validate required columns and data shape. Returns (error_message or None).

    Only enforce Timestamp presence/shape. If customization specifies measured/target
    columns, validate their existence; otherwise skip optional fields.
    """
    if df is None:
        return "Parsed dataframe is empty."

    if "Timestamp" not in df.columns:
        return "Missing required 'Timestamp' column."

    timestamps = df["Timestamp"].to_numpy(copy=False)
    if timestamps.size == 0 or not ((timestamps[1:] > timestamps[:-1]).all()):
        return "Timestamp column must be strictly increasing."

    if customization and isinstance(customization, dict):
        # Validate explicitly declared measured/target columns if provided
        declared = set()
        for col in (customization.get("measured") or []):
            declared.add(col)
        for col in (customization.get("target") or []):
            declared.add(col)

        devices = customization.get("devices") or []
        for device in devices:
            if not isinstance(device, dict):
                continue
            m = device.get("measured")
            t = device.get("target")
            if m:
                declared.add(m)
            if t:
                declared.add(t)

        missing = [c for c in declared if c and c not in df.columns]
        if missing:
            return f"Missing columns declared in customization: {', '.join(missing)}"

    return None
