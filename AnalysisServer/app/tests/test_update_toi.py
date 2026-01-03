import pathlib
import sys

import pandas as pd
from fastapi import FastAPI
from fastapi.testclient import TestClient

# Ensure the AnalysisServer package is on the path for imports
ROOT_DIR = pathlib.Path(__file__).resolve().parents[2]
if str(ROOT_DIR) not in sys.path:
    sys.path.insert(0, str(ROOT_DIR))

from app.core import analyzer
from app.core.session_cache import store_session
from app.routers import analyze


def test_detect_toi_changes_basic():
    df = pd.DataFrame({
        "Timestamp": [0.0, 1.0, 2.0],
        "A": [0.0, 10.0, 0.0],
    })

    rows = analyzer.detect_toi_changes(
        df,
        column="A",
        ts_col="Timestamp",
        direction="either",
        threshold_mode="percent",
        threshold_value=50.0,
        min_spacing_ms=500,
        max_rows=5,
    )

    assert len(rows) == 2
    assert list(rows[0].keys()) == ["time"]
    assert rows[0]["time"] == 1.0
    assert rows[1]["time"] == 2.0


def test_update_toi_settings_endpoint_returns_rows():
    df = pd.DataFrame({
        "Timestamp": [0.0, 1.0, 2.0],
        "A": [0.0, 10.0, 0.0],
    })
    session_id = store_session(df, {"subplots": []}, {"timestamp": "Timestamp"})

    app = FastAPI()
    app.include_router(analyze.router)
    client = TestClient(app)

    payload = {
        "sessionId": session_id,
        "subplots": [
            {
                "subplotIndex": 0,
                "column": "A",
                "direction": "either",
                "thresholdMode": "percent",
                "thresholdValue": 50.0,
                "minSpacingMs": 500,
                "maxRows": 5,
            }
        ],
    }

    res = client.post("/updateToiSettings", json=payload)
    assert res.status_code == 200, res.text
    data = res.json()
    assert "subplots" in data
    assert len(data["subplots"]) == 1
    rows = data["subplots"][0]["rows"]
    assert len(rows) == 2
    assert list(rows[0].keys()) == ["time"]
    assert rows[0]["time"] == 1.0
    assert rows[1]["time"] == 2.0
