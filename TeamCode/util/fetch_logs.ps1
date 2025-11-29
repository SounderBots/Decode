# Script to fetch CSV logs from Control Hub
# Usage: .\fetch_logs.ps1 [-SavePath "C:\My\Logs"]

param (
    [string]$SavePath = (Get-Location).Path
)

Write-Host "Searching for logs on Control Hub..."

# 1. Try to find the App-Specific External Storage path (where DataLogger saves now)
# We use a wildcard because the SD card UUID (e.g., 4A1D-1156) changes per card.
$remotePath = adb shell "ls -d /storage/*/Android/data/com.qualcomm.ftcrobotcontroller/files/logs 2>/dev/null"

# 2. If that returns nothing or error, check the legacy internal path
if (-not $remotePath -or $remotePath -match "No such file") {
    Write-Host "External path not found, checking internal storage..."
    $remotePath = "/sdcard/FIRST/data/logs"
}

# Clean up the path string (remove whitespace/newlines)
$remotePath = $remotePath.Trim()

if (-not $remotePath -or $remotePath -match "No such file") {
    Write-Error "Could not find any log directories on the Control Hub."
    exit 1
}

Write-Host "Found logs at: $remotePath"
Write-Host "Downloading to: $SavePath"

# Create destination directory if it doesn't exist
if (!(Test-Path $SavePath)) {
    New-Item -ItemType Directory -Force -Path $SavePath | Out-Null
}

# Get list of files to print later
$files = adb shell ls "$remotePath"
$fileList = $files -split "[\r\n]+" | Where-Object { $_ -ne "" }

Write-Host "`nProcessing files..."

foreach ($file in $fileList) {
    $localPath = Join-Path -Path $SavePath -ChildPath $file
    
    if (Test-Path $localPath) {
        Write-Host "Skipping (Already Exists): $localPath" -ForegroundColor Yellow
    } else {
        # Pull specific file
        adb pull "$remotePath/$file" "$SavePath" | Out-Null
        Write-Host "Pulled: $localPath" -ForegroundColor Green
    }
}

Write-Host "Download complete."
