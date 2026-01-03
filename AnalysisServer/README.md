# AnalysisServer

This is a FastAPI-based web application for analyzing FTC Decode robot logs.

## Structure

- `app/`: Main application code
  - `core/`: Business logic (parsing, analysis, visualization)
  - `routers/`: API endpoints
  - `templates/`: HTML templates
  - `static/`: CSS and JS assets

## Running Locally (Windows-friendly)

1. Ensure Python 3.11 is installed (64-bit). Quick install: `winget install -e --id Python.Python.3.11`.
2. From `AnalysisServer`, run the setup script (creates/refreshes `.venv`, installs deps):
   ```powershell
   .\init.ps1
   ```
3. Start the server (granian):
   ```powershell
   .\runlocal.ps1
   ```
4. Open http://127.0.0.1:8000

### Troubleshooting local ports
- If `runlocal.ps1` says the port is in use, stop any old instances (Ctrl+C in the running terminal, or `Get-NetTCPConnection -LocalPort 8000` and kill the owning process), then rerun.
- For Docker, each `docker run` holds its host port; stop stray containers with `docker ps` then `docker stop <id>`. Map a different host port if 8000 is busy (e.g., `-p 8001:8000`).

## Deployment

This project includes a Dockerfile for deployment to Azure App Service (Linux container, Python 3.11 + granian). It honors `PORT` and `WORKERS` env vars (defaults: 80 and `nproc`).

## Local container test (Docker Desktop)

1. Install Docker Desktop for Windows (https://www.docker.com/products/docker-desktop/) and start it.
2. Build the image (from `AnalysisServer`):
   ```bash
   docker build -t decode-analysis .
   ```
3. Run the container locally. If host port 8000 is free:
   ```bash
   docker run --rm -e PORT=8000 -e WORKERS=2 -p 8000:8000 decode-analysis
   ```
   If 8000 is already in use (common on Docker Desktop), map to an alternate host port (app still listens on 8000 inside the container):
   ```bash
   docker run --rm -e PORT=8000 -e WORKERS=2 -p 8001:8000 decode-analysis
   ```
   To quickly see what owns 8000 on Windows:
   ```powershell
   Get-NetTCPConnection -LocalPort 8000 | Select-Object LocalAddress,LocalPort,State,OwningProcess
   ```
   To stop a stray container that is holding the port:
   ```powershell
   docker ps --format "{{.ID}} {{.Ports}} {{.Names}}"
   docker stop <container_id_or_name>
   ```
4. Open http://127.0.0.1:<host-port> (8000 or 8001), upload a CSV, and verify the flow. Logs appear in container stdout/stderr.
