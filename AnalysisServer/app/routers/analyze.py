import json
import logging
from pathlib import Path
from typing import List

from fastapi import APIRouter, UploadFile, File, Request, Form
from fastapi.responses import JSONResponse
from fastapi.templating import Jinja2Templates
from pydantic import BaseModel, Field, field_validator

from app.core import parser, analyzer, visualizer
from app.core.session_cache import store_session, get_session

router = APIRouter()

# Get the base directory (AnalysisServer/app)
# analyze.py is in app/routers, so we go up one level
BASE_DIR = Path(__file__).resolve().parent.parent

templates = Jinja2Templates(directory=str(BASE_DIR / "templates"))

logger = logging.getLogger("analyze")
logging.basicConfig(level=logging.INFO)

SESSION_TTL_SECONDS = 30 * 60  # kept for reference in this module


class SubplotSettings(BaseModel):
    subplotIndex: int = Field(..., ge=0)
    column: str
    direction: str = Field(..., description="rising | falling | either")
    thresholdMode: str = Field(..., description="percent | absolute")
    thresholdValue: float
    minSpacingMs: float = 500
    maxRows: int = 50

    @field_validator("direction")
    def _validate_direction(cls, v):
        if v not in {"rising", "falling", "either"}:
            raise ValueError("direction must be rising, falling, or either")
        return v

    @field_validator("thresholdMode")
    def _validate_mode(cls, v):
        if v not in {"percent", "absolute"}:
            raise ValueError("thresholdMode must be percent or absolute")
        return v

    @field_validator("maxRows")
    def _validate_max_rows(cls, v):
        if v is None or v <= 0:
            return 50
        return v


class UpdateToiRequest(BaseModel):
    sessionId: str
    subplots: List[SubplotSettings]

def _client_ip(request: Request) -> str:
    xff = request.headers.get("x-forwarded-for")
    if xff:
        return xff.split(",")[0].strip()
    return request.client.host if request.client else ""

@router.post("/analyze")
async def analyze_log(
    request: Request,
    file: UploadFile = File(...),
    customization: str = Form(None),
    assume_periodic: str = Form(None),
):
    content = await file.read()
    client_ip = _client_ip(request)
    file_size = len(content) if content else 0

    # --- Validation ---
    max_size_bytes = 3 * 1024 * 1024
    if len(content) > max_size_bytes:
        logger.info(json.dumps({
            "event": "upload_rejected",
            "reason": "too_large",
            "client_ip": client_ip,
            "file_name": file.filename,
            "content_length": file_size
        }))
        return templates.TemplateResponse("index.html", {"request": request, "error": "File too large. Limit is 3 MB."})

    csv_mime_types = {"text/csv", "application/csv", "application/vnd.ms-excel", ""}
    has_csv_ext = bool(file.filename and file.filename.lower().endswith(".csv"))
    has_csv_mime = file.content_type in csv_mime_types
    if not (has_csv_ext or has_csv_mime):
        logger.info(json.dumps({
            "event": "upload_rejected",
            "reason": "not_csv",
            "client_ip": client_ip,
            "file_name": file.filename,
            "content_type": file.content_type,
            "content_length": file_size
        }))
        return templates.TemplateResponse("index.html", {"request": request, "error": "Only CSV files are accepted."})

    df, metadata_lines, parse_error = parser.parse_log_file(content)
    if parse_error:
        logger.info(json.dumps({
            "event": "upload_rejected",
            "reason": "parse_error",
            "client_ip": client_ip,
            "file_name": file.filename,
            "content_length": file_size,
            "detail": parse_error
        }))
        return templates.TemplateResponse("index.html", {"request": request, "error": parse_error})

    # Run schema validation using customization when available to enforce declared columns
    schema_error = parser.validate_log_dataframe(df, customization_payload if 'customization_payload' in locals() else None)
    if schema_error:
        logger.info(json.dumps({
            "event": "upload_rejected",
            "reason": "schema_error",
            "client_ip": client_ip,
            "file_name": file.filename,
            "content_length": file_size,
            "detail": schema_error
        }))
        return templates.TemplateResponse("index.html", {"request": request, "error": schema_error})

    logger.info(json.dumps({
        "event": "upload_validated",
        "client_ip": client_ip,
        "file_name": file.filename,
        "content_length": file_size,
        "metadata_lines": len(metadata_lines)
    }))
    
    def build_default_customization(columns: list[str]) -> dict:
        data_columns = [c for c in columns if c != "Timestamp"]
        return {
            "columns": list(columns),
            "devices": [],
            "events": [],
            "subplots": [
                {
                    "title": "Log Data",
                    "columns": data_columns,
                    "shareY": False,
                    "diff": False,
                    "interest": False,
                    "legendOverrides": [],
                }
            ],
        }

    customization_payload = None
    if customization:
        try:
            customization_payload = json.loads(customization)
            logger.info(json.dumps({
                "event": "customization_received",
                "client_ip": client_ip,
                "file_name": file.filename,
                "bytes": len(customization),
                "subplots": len(customization_payload.get("subplots", [])) if isinstance(customization_payload, dict) else None
            }))
        except Exception:
            logger.exception(json.dumps({
                "event": "customization_parse_failed",
                "client_ip": client_ip,
                "file_name": file.filename
            }))
            customization_payload = None

    if not customization_payload or not isinstance(customization_payload, dict):
        customization_payload = build_default_customization(list(df.columns))

    interest_subplots = []
    for i, sp in enumerate(customization_payload.get("subplots", []) or []):
        if sp.get("interest"):
            interest_subplots.append({
                "index": i,
                "title": sp.get("title") or f"Subplot {i+1}",
                "interest_title": (sp.get("interestTitle") or sp.get("interest_title") or "").strip() or None,
            })

    # --- Analysis ---
    column_map = analyzer.resolve_column_mappings(df, customization_payload)
    session_id = store_session(df, customization_payload, column_map)

    # 1. Loop Stats (optional when rows are known periodic)
    loop_stats_enabled = bool(assume_periodic)
    loop_stats = analyzer.calculate_loop_stats(df, column_map=column_map) if loop_stats_enabled else None
    
    # TOI payload is intentionally empty; detection runs via /updateToiSettings on demand
    toi_payload = {"rows": []}
    
    # --- Visualization ---
    # We pass the detected shot times and unstable times to the visualizer
    custom_plot = None
    try:
        custom_plot = visualizer.create_custom_plot(df, customization_payload)
        logger.info(json.dumps({
            "event": "custom_plot_built",
            "client_ip": client_ip,
            "file_name": file.filename,
            "traces": len(custom_plot.get("data", [])) if isinstance(custom_plot, dict) else None,
            "subplots": len(customization_payload.get("subplots", [])) if isinstance(customization_payload, dict) else None
        }))
    except Exception:
        logger.exception(json.dumps({
            "event": "custom_plot_failed",
            "client_ip": client_ip,
            "file_name": file.filename
        }))
        custom_plot = None

    if not custom_plot or not isinstance(custom_plot, dict) or not custom_plot.get("data"):
        try:
            fallback_payload = build_default_customization(list(df.columns))
            custom_plot = visualizer.create_custom_plot(df, fallback_payload)
            logger.info(json.dumps({
                "event": "custom_plot_built_fallback",
                "client_ip": client_ip,
                "file_name": file.filename,
                "traces": len(custom_plot.get("data", [])) if isinstance(custom_plot, dict) else None,
                "subplots": len(fallback_payload.get("subplots", [])),
            }))
        except Exception:
            logger.exception(json.dumps({
                "event": "custom_plot_fallback_failed",
                "client_ip": client_ip,
                "file_name": file.filename
            }))
            custom_plot = None

    analysis_plot = custom_plot or {"data": [], "layout": {}}
    
    logger.info(json.dumps({
        "event": "upload_accepted",
        "client_ip": client_ip,
        "file_name": file.filename,
        "content_length": file_size,
        "loop_stats_present": bool(loop_stats),
        "shots_detected": 0,
        "analysis_ready": bool(analysis_plot)
    }))

    return templates.TemplateResponse("report.html", {
        "request": request,
        "filename": file.filename,
        "file_size_kb": round(file_size / 1024, 1) if file_size else None,
        "loop_stats": loop_stats,
        "loop_stats_enabled": loop_stats_enabled,
        "toi_payload": toi_payload,
        "interest_subplots": interest_subplots,
        "analysis_plot": analysis_plot,
        "custom_plot": custom_plot,
        "metadata_lines": metadata_lines,
        "customization_payload": customization_payload,
        "session_id": session_id,
    })


@router.post("/updateToiSettings")
async def update_toi_settings(payload: UpdateToiRequest):
    session = get_session(payload.sessionId)
    if not session:
        return JSONResponse({"error": "Session expired or invalid."}, status_code=400)

    df = session.get("df")
    column_map = session.get("column_map") or {}
    ts_col = column_map.get("timestamp") or "Timestamp"

    if df is None or ts_col not in df.columns:
        return JSONResponse({"error": "Session data is missing required columns."}, status_code=400)

    warnings = []
    resp_subplots = []

    for sp in payload.subplots or []:
        rows = []
        if not sp.column or sp.column not in df.columns:
            warnings.append(f"Subplot {sp.subplotIndex}: column '{sp.column}' not found.")
        else:
            max_rows = sp.maxRows if sp.maxRows else 50
            if max_rows <= 0:
                max_rows = 50
            max_rows = min(max_rows, 50)
            try:
                rows = analyzer.detect_toi_changes(
                    df,
                    sp.column,
                    ts_col=ts_col,
                    direction=sp.direction,
                    threshold_mode=sp.thresholdMode,
                    threshold_value=sp.thresholdValue,
                    min_spacing_ms=sp.minSpacingMs,
                    max_rows=max_rows,
                )
            except Exception as exc:
                logger.exception(json.dumps({
                    "event": "update_toi_detection_failed",
                    "session_id": payload.sessionId,
                    "subplot_index": sp.subplotIndex,
                    "column": sp.column
                }))
                warnings.append(f"Subplot {sp.subplotIndex}: detection failed ({exc}).")
                rows = []

        # Do not include positional indices; client can infer from list order
        resp_subplots.append({"index": sp.subplotIndex, "rows": rows})

    logger.info(json.dumps({
        "event": "update_toi_settings",
        "session_id": payload.sessionId,
        "subplot_count": len(resp_subplots),
        "warnings": warnings,
        "rows_returned": [len(sp.get("rows", [])) for sp in resp_subplots],
    }))

    return {"subplots": resp_subplots, "warnings": warnings}
