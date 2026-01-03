import threading
import time
import uuid

# Lightweight in-memory session cache; keeps parsed dataframe and customization
# for a short time so TOI updates can reuse server-side data without re-upload.

SESSION_TTL_SECONDS = 30 * 60  # 30 minutes
_SESSION_CACHE: dict[str, dict] = {}
_SESSION_LOCK = threading.Lock()


def _cleanup_sessions(now: float | None = None):
    now = now or time.time()
    with _SESSION_LOCK:
        expired = [sid for sid, payload in _SESSION_CACHE.items() if now - payload.get("created", 0) > SESSION_TTL_SECONDS]
        for sid in expired:
            _SESSION_CACHE.pop(sid, None)


def store_session(df, customization_payload, column_map) -> str:
    """Persist parsed artifacts in-process and return a session token."""
    session_id = str(uuid.uuid4())
    created = time.time()
    with _SESSION_LOCK:
        _SESSION_CACHE[session_id] = {
            "created": created,
            "df": df,
            "customization": customization_payload,
            "column_map": column_map,
        }
    _cleanup_sessions(now=created)
    return session_id


def get_session(session_id: str):
    """Retrieve session payload if present and not expired."""
    now = time.time()
    with _SESSION_LOCK:
        payload = _SESSION_CACHE.get(session_id)
        if not payload:
            return None
        if now - payload.get("created", 0) > SESSION_TTL_SECONDS:
            _SESSION_CACHE.pop(session_id, None)
            return None
        return payload
