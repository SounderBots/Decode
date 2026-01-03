# AnalysisServer

This is a FastAPI-based web application for analyzing FTC DataLogger logs.

## Structure

- `app/`: Main application code
  - `core/`: Business logic (parsing, analysis, visualization)
  - `routers/`: API endpoints
  - `templates/`: HTML templates
  - `static/`: CSS and JS assets

## Running Locally (Windows-friendly)

1. Ensure Python 3.11 is installed (64-bit).
   - Install via winget: `winget install -e --id Python.Python.3.11`
   - Download installer: https://www.python.org/ftp/python/3.11.9/python-3.11.9-amd64.exe
   - Or visit: https://www.python.org/downloads/release/python-3110/
2. From `AnalysisServer`, run the setup script (creates/refreshes `.venv`, installs deps):
   ```powershell
   .\init.ps1
   ```
3. Start the server (granian):
   ```powershell
   .\runlocal.ps1
   ```
4. Open http://127.0.0.1:8000

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
   Get-NetTCPConnection -LocalPort 8000
   ```

### Troubleshooting local ports
- If `runlocal.ps1` says the port is in use, stop any old instances (Ctrl+C in the running terminal, or `Get-NetTCPConnection -LocalPort 8000` and kill the owning process), then rerun.
- For Docker, each `docker run` holds its host port; stop stray containers with `docker ps` then `docker stop <id>`. Map a different host port if 8000 is busy (e.g., `-p 8001:8000`).

## Deployment

This project includes a Dockerfile for deployment to Azure App Service (Linux container, Python 3.11 + granian). It honors `PORT` and `WORKERS` env vars (defaults: 80 and `nproc`).

## Azure Deployment via GitHub Actions

This repository includes a GitHub Actions workflow (`.github/workflows/deploy-analysisserver.yml`) and Bicep infrastructure code (`AnalysisServer/infra/`) to automatically deploy the application to Azure.

### Prerequisites

1.  **Azure Subscription**: You need an active Azure subscription.
2.  **Resource Group**: Create a Resource Group in Azure (e.g., `SounderBots-Analysis-RG`).
3.  **App Registration (OIDC)**:
    *   Create an App Registration in Azure AD.
    *   Under "Certificates & secrets" -> "Federated credentials", add a new credential.
    *   Select "GitHub Actions deploying Azure resources".
    *   Enter your Organization, Repository, and set Entity type to "Branch" (Name: `main`).
    *   **Important**: Go to your Resource Group in the Azure Portal, select **Access control (IAM)** > **Add role assignment**. Assign the **Contributor** role to the App Registration you just created. This is required for Bicep to create resources.

### GitHub Secrets Configuration

Go to your GitHub Repository -> Settings -> Secrets and variables -> Actions -> New repository secret. Add the following:

*   `AZURE_CLIENT_ID`: The Application (client) ID of your App Registration.
*   `AZURE_TENANT_ID`: The Directory (tenant) ID of your Azure AD.
*   `AZURE_SUBSCRIPTION_ID`: Your Azure Subscription ID.
*   `AZURE_RESOURCE_GROUP`: The name of the Resource Group you created (e.g., `SounderBots-Analysis-RG`).

### Deployment

This workflow is configured to run **manually** (on-demand) and uses the latest Azure Actions (v2) and Docker Buildx.

1.  Go to the **Actions** tab in your GitHub repository.
2.  Select the **Deploy Analysis Server** workflow from the left sidebar.
3.  Click the **Run workflow** button.
4.  Select the branch (usually `main`) and click **Run workflow**.

The workflow will:
1.  **Infrastructure as Code**: Provision/Update Azure resources (ACR, App Service Plan, Web App) using Bicep.
2.  **Build**: Build the Docker image using Docker Buildx.
3.  **Push**: Push the image to the Azure Container Registry.
4.  **Deploy**: Update the Web App to run the new image.

### Accessing the App

After a successful deployment, the URL to your Web App will be displayed in the **workflow run summary** (look for the "Web App URL" notice) or in the logs of the "Show Web App URL" step.

The URL format will be: `https://datalogger-analysis-<unique_suffix>.azurewebsites.net`
