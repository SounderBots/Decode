$ScriptDir    = Split-Path -Parent $MyInvocation.MyCommand.Definition
$ProjectRoot  = Split-Path -Parent $ScriptDir
$VenvPath     = Join-Path $ProjectRoot ".venv"
$VenvPython   = Join-Path $VenvPath "Scripts\python.exe"
$Requirements = Join-Path $ScriptDir "requirements.txt"

function Resolve-Python311 {
    $candidates = @(
        "py -3.11",
        "python3.11",
        "python311",
        "python"
    )
    foreach ($cmd in $candidates) {
        try {
            & $cmd -V *> $null
            if ($LASTEXITCODE -eq 0) {
                $version = (& $cmd -V).Trim()
                if ($version -match "3\.11") { return $cmd }
            }
        } catch {}
    }
    return $null
}

$pyCmd = Resolve-Python311
if (-not $pyCmd) {
    throw "Python 3.11 not found. Install it (64-bit) via winget: winget install -e --id Python.Python.3.11, or from https://www.python.org/downloads/release/python-3110/, then rerun init.ps1."
}

if (Test-Path $VenvPath) {
    try {
        $venvVersion = & $VenvPython -c "import sys; print(f'{sys.version_info.major}.{sys.version_info.minor}')"
    } catch {
        $venvVersion = "unknown"
    }

    if ($venvVersion -ne "3.11") {
        Write-Host "Existing venv is not Python 3.11 (found $venvVersion). Recreating with Python 3.11..."
        Remove-Item -Recurse -Force $VenvPath
        Write-Host "Creating virtual environment with: $pyCmd"
        & $pyCmd -m venv $VenvPath
    } else {
        Write-Host "Using existing venv (Python $venvVersion): $VenvPath"
    }
} else {
    Write-Host "Creating virtual environment with: $pyCmd"
    & $pyCmd -m venv $VenvPath
}

Write-Host "Upgrading pip..."
& $VenvPython -m pip install --upgrade pip

Write-Host "Installing dependencies..."
& $VenvPython -m pip install -r $Requirements

Write-Host "Environment ready. Activate with `\.\.\venv\Scripts\Activate.ps1` or run .\runlocal.ps1 to start."
