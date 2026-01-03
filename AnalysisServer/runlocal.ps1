$ScriptDir    = Split-Path -Parent $MyInvocation.MyCommand.Definition
$ProjectRoot  = Split-Path -Parent $ScriptDir
$VenvPath     = Join-Path $ProjectRoot ".venv"
$VenvPython   = Join-Path $VenvPath "Scripts\python.exe"

if (-not (Test-Path $VenvPython)) {
	throw "Virtual env not found. Run .\init.ps1 first (requires Python 3.11) then rerun this script."
}

# Set PYTHONPATH to the AnalysisServer directory so imports work correctly
$env:PYTHONPATH = $ScriptDir
# Default log directory outside watched app folder to avoid reload loops
if (-not $env:LOG_DIR) { $env:LOG_DIR = (Join-Path $ProjectRoot "logs") }
# Ensure log directory exists and is visible in output
New-Item -ItemType Directory -Force -Path $env:LOG_DIR | Out-Null
Write-Host "LOG_DIR: $env:LOG_DIR"

# Stop any stale granian instances from this venv to avoid serving old code
$stale = Get-Process -Name python -ErrorAction SilentlyContinue |
	Where-Object { $_.Path -eq $VenvPython -and $_.CommandLine -match "granian" }
if ($stale) {
	Write-Host "Stopping stale granian processes..."
	$stale | ForEach-Object { try { Stop-Process -Id $_.Id -Force -ErrorAction Stop } catch {} }
}

Write-Host "Starting AnalysisServer locally..."
Write-Host "Python Executable: $VenvPython"
Write-Host "PYTHONPATH: $env:PYTHONPATH"

# Run Granian with reload
& $VenvPython -m granian --interface asgi --host 127.0.0.1 --port 8000 app.main:app --reload
$LASTEXITCODE