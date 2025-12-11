# Script to fetch System Logs (logcat) from Control Hub
# Usage: .\fetch_system_logs.ps1

param (
    [string]$SavePath = (Get-Location).Path
)

# --- Pre-flight Checks ---
if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
    Write-Error "Error: 'adb' command not found. Please install Android Platform Tools and add it to your PATH."
    exit 1
}

# --- Device Selection Logic ---
# Force array result to handle single/multiple/zero matches consistently
# Use \s+ to match either tabs or spaces. Allow trailing whitespace.
$devices = @(adb devices | Select-String -Pattern "\s+device\s*$")

if ($devices.Count -eq 0) {
    Write-Error "No ADB devices connected!"
    exit 1
}

$adbSerial = ""

if ($devices.Count -eq 1) {
    # Always extract serial to be safe against other offline devices
    $adbSerial = $devices[0].Line.Trim() -split '\s+' | Select-Object -First 1
}
elseif ($devices.Count -gt 1) {
    # Filter out emulators
    $realDevice = $devices | Where-Object { $_.Line -notmatch "emulator" } | Select-Object -First 1
    if ($realDevice) {
        $adbSerial = $realDevice.Line.Trim() -split '\s+' | Select-Object -First 1
        Write-Host "Multiple devices found. Selecting physical device: $adbSerial"
    } else {
        $adbSerial = $devices[0].Line.Trim() -split '\s+' | Select-Object -First 1
        Write-Host "Multiple devices found. Selecting first device: $adbSerial"
    }
}
# ------------------------------

# --- Path Validation ---
# Ensure output directory exists and is actually a directory
if (Test-Path $SavePath) {
    if (-not (Get-Item $SavePath).PSIsContainer) {
        Write-Error "Error: The path '$SavePath' is a file. Please provide a directory path."
        exit 1
    }
} else {
    New-Item -ItemType Directory -Force -Path $SavePath | Out-Null
}

# Resolve to absolute path to ensure cmd.exe handles it correctly
$absSavePath = (Get-Item $SavePath).FullName
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$fileName = "logcat_$timestamp.txt"
$fullPath = Join-Path $absSavePath $fileName

Write-Host "Fetching system logs (logcat) from device $adbSerial..."

# Use cmd.exe for redirection to avoid PowerShell encoding issues and improve speed
# Added -b all to capture radio and event buffers which are useful for connectivity issues
# Note: $adbSerial is guaranteed to be set by the logic above
cmd /c "adb -s $adbSerial logcat -d -v threadtime -b all > ""$fullPath"""

if ($LASTEXITCODE -eq 0) {
    Write-Host "System log saved to: $fullPath"
} else {
    Write-Error "Failed to fetch logs. Please check ADB connection."
}

# --- Fetch Persistent Log (Survives Reboots) ---
$persistentLogName = "robotControllerLog_$timestamp.txt"
$persistentLogPath = Join-Path $absSavePath $persistentLogName

Write-Host "Fetching persistent Robot Controller log..."

# List of possible locations for the log file (varies by Android version)
$possibleLogPaths = @(
    "/sdcard/FIRST/robotControllerLog.txt",
    "/sdcard/Android/data/com.qualcomm.ftcrobotcontroller/files/robotControllerLog.txt",
    "/storage/emulated/0/Android/data/com.qualcomm.ftcrobotcontroller/files/robotControllerLog.txt"
)

$logFound = $false

foreach ($remotePath in $possibleLogPaths) {
    # Check if file exists using shell command
    # Use direct execution instead of Invoke-Expression for safety and better output handling
    $checkOutput = if ($adbSerial) { 
        adb -s $adbSerial shell "[ -f $remotePath ] && echo FOUND" 2>$null
    } else { 
        adb shell "[ -f $remotePath ] && echo FOUND" 2>$null
    }
    
    # Check if output contains "FOUND" (handles potential whitespace or extra lines)
    if ($checkOutput -match "FOUND") {
        Write-Host "Found log at: $remotePath"
        
        if ($adbSerial) {
            cmd /c "adb -s $adbSerial pull ""$remotePath"" ""$persistentLogPath"""
        } else {
            cmd /c "adb pull ""$remotePath"" ""$persistentLogPath"""
        }

        if ($LASTEXITCODE -eq 0) {
            Write-Host "Persistent log saved to: $persistentLogPath"
            $logFound = $true
            break
        }
    }
}

if (-not $logFound) {
    Write-Warning "Could not find 'robotControllerLog.txt' in standard locations."
    Write-Warning "This file may not exist if the app hasn't crashed or logged significantly yet."
}
# -----------------------------------------------

Write-Host " "
Write-Host "Tips for analysis:"
Write-Host "1. Use 'logcat_*.txt' for detailed Wi-Fi/Radio events (must fetch BEFORE reboot)."
Write-Host "2. Use 'robotControllerLog_*.txt' if the robot was rebooted (survives power cycle)."
Write-Host "3. Search for 'WifiStateMachine' or 'disconnect' in either file."
