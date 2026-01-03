import json
import logging
import os
import sys
import time
from pathlib import Path

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates

from app.routers import analyze

# Paths
BASE_DIR = Path(__file__).resolve().parent  # AnalysisServer/app
PROJECT_ROOT = BASE_DIR.parent

# Simple logging similar to the original working setup: stdout only, no file handler.
LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO").upper()
log_fmt = "%(asctime)s %(levelname)s [%(name)s] %(message)s"
logging.basicConfig(level=LOG_LEVEL, format=log_fmt, handlers=[logging.StreamHandler(sys.stdout)])
root_logger = logging.getLogger()
root_logger.info("logging_initialized level=%s", LOG_LEVEL)

app = FastAPI(title="FTC 23270 Log Analysis")

# Mount static files
app.mount("/static", StaticFiles(directory=str(BASE_DIR / "static")), name="static")

# Setup templates
templates = Jinja2Templates(directory=str(BASE_DIR / "templates"))

logger = logging.getLogger("index")
request_log = logging.getLogger("request")
request_log.setLevel(root_logger.level)
logger.info("startup_index_logger_ready")


def _client_ip(request: Request) -> str:
    xff = request.headers.get("x-forwarded-for")
    if xff:
        return xff.split(",")[0].strip()
    return request.client.host if request.client else ""


# Lightweight request logger to see inbound traffic in the terminal.
@app.middleware("http")
async def request_logger(request: Request, call_next):
    start = time.time()
    client_ip = _client_ip(request)
    path = request.url.path
    try:
        response = await call_next(request)
    except Exception:
        request_log.exception("request_failed method=%s path=%s ip=%s", request.method, path, client_ip)
        raise

    duration_ms = int((time.time() - start) * 1000)
    # Log to stderr for visibility even if logging config is broken; also lands in server.log via file handler.
    print(f"[req] {request.method} {path} ip={client_ip} status={response.status_code} {duration_ms}ms", file=sys.stderr, flush=True)
    request_log.info(
        json.dumps({
            "event": "request",
            "method": request.method,
            "path": path,
            "status": response.status_code,
            "client_ip": client_ip,
            "duration_ms": duration_ms,
        })
    )
    return response

# Include routers
app.include_router(analyze.router)


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception):
    request_log.exception("unhandled_exception path=%s", request.url.path)
    return JSONResponse({"error": "Internal server error"}, status_code=500)

@app.get("/")
async def root(request: Request):
    logger.info(json.dumps({
        "event": "page_view",
        "path": "/",
        "client_ip": _client_ip(request),
        "user_agent": request.headers.get("user-agent", "")
    }))
    return templates.TemplateResponse("index.html", {"request": request})


@app.get("/health")
async def health():
    return {"status": "ok"}
