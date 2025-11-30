# Script to fetch CSV logs from Control Hub
# Usage: .\fetch_logs.ps1 [-SavePath "C:\My\Logs"]

param (
    [string]$SavePath = (Get-Location).Path
)

# --- Device Selection Logic ---
$devices = adb devices | Select-String -Pattern "\tdevice$"
if ($devices.Count -eq 0) {
    Write-Error "No ADB devices connected!"
    exit 1
}

$adbSerial = ""
if ($devices.Count -gt 1) {
    # Filter out emulators
    $realDevice = $devices | Where-Object { $_ -notmatch "emulator" } | Select-Object -First 1
    if ($realDevice) {
        $adbSerial = $realDevice.ToString().Split("`t")[0].Trim()
        Write-Host "Multiple devices found. Selecting physical device: $adbSerial"
    } else {
        $adbSerial = $devices[0].ToString().Split("`t")[0].Trim()
        Write-Host "Multiple devices found. Selecting first device: $adbSerial"
    }
}
# ------------------------------

Write-Host "Searching for logs on Control Hub..."

# 1. Try to find the App-Specific External Storage path (where DataLogger saves now)
if ($adbSerial) {
    $remotePath = adb -s $adbSerial shell "ls -d /storage/*/Android/data/com.qualcomm.ftcrobotcontroller/files/logs 2>/dev/null"
} else {
    $remotePath = adb shell "ls -d /storage/*/Android/data/com.qualcomm.ftcrobotcontroller/files/logs 2>/dev/null"
}

# Handle case where multiple paths are returned (e.g. multiple storage devices)
if ($remotePath -is [array]) {
    $remotePath = $remotePath | Select-Object -First 1
}

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

# Get list of files
if ($adbSerial) {
    $files = adb -s $adbSerial shell ls "$remotePath"
} else {
    $files = adb shell ls "$remotePath"
}

$fileList = $files -split "[\r\n]+" | Where-Object { $_ -ne "" -and $_ -like "*.csv" }

if ($fileList.Count -eq 0) {
    Write-Host "No CSV logs found."
    exit 0
}

Write-Host "`nProcessing $($fileList.Count) files..."

foreach ($file in $fileList) {
    $localPath = Join-Path -Path $SavePath -ChildPath $file
    
    if (Test-Path $localPath) {
        Write-Host "Skipping (Already Exists): $localPath" -ForegroundColor Yellow
    } else {
        # Pull specific file
        if ($adbSerial) {
            adb -s $adbSerial pull "$remotePath/$file" "$SavePath" | Out-Null
        } else {
            adb pull "$remotePath/$file" "$SavePath" | Out-Null
        }
        Write-Host "Pulled: $localPath" -ForegroundColor Green
    }
}

Write-Host "Download complete."
