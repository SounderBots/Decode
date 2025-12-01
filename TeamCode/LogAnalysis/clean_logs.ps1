# Script to clean up log files from Control Hub (Internal Storage & SD Card)
# Usage: .\clean_logs.ps1 [-Force]

param (
    [switch]$Force # Skip confirmation
)

# --- Device Selection Logic ---
$devicesRaw = adb devices | Select-String -Pattern "\tdevice$"
$deviceList = @()

if ($devicesRaw) {
    foreach ($line in $devicesRaw) {
        $serial = $line.ToString().Split("`t")[0].Trim()
        $deviceList += $serial
    }
}

if ($deviceList.Count -eq 0) {
    Write-Error "No ADB devices connected!"
    exit 1
}

$adbSerial = ""

if ($deviceList.Count -eq 1) {
    $adbSerial = $deviceList[0]
    Write-Host "Found single device: $adbSerial"
} else {
    Write-Host "Multiple devices found:"
    for ($i = 0; $i -lt $deviceList.Count; $i++) {
        Write-Host "[$($i+1)] $($deviceList[$i])"
    }
    
    do {
        $selection = Read-Host "Select device number (1-$($deviceList.Count))"
    } until ($selection -match "^\d+$" -and [int]$selection -ge 1 -and [int]$selection -le $deviceList.Count)
    
    $adbSerial = $deviceList[([int]$selection - 1)]
    Write-Host "Selected: $adbSerial"
}
# ------------------------------

$adbCmd = if ($adbSerial) { "adb -s $adbSerial" } else { "adb" }

Write-Host "Scanning for log directories on device..."

# List of potential patterns to search for log folders
# 1. Standard internal storage (legacy/custom)
# 2. App-specific storage (Internal & External SD)
$searchPatterns = @(
    "/sdcard/FIRST/data/logs",
    "/storage/*/Android/data/com.qualcomm.ftcrobotcontroller/files/logs"
)

$foundPaths = @()

foreach ($pattern in $searchPatterns) {
    # Use ls -d to find directories matching the pattern
    # We wrap the command in quotes so the redirection happens on the Android side, not locally
    $cmd = "$adbCmd shell ""ls -d $pattern 2>/dev/null"""
    $result = Invoke-Expression $cmd
    
    if ($result) {
        # Split by newline in case multiple paths match (e.g. internal + sd card)
        $paths = $result -split "`r`n" | Where-Object { $_ -ne "" }
        $foundPaths += $paths
    }
}

# Remove duplicates
$foundPaths = $foundPaths | Select-Object -Unique

if ($foundPaths.Count -eq 0) {
    Write-Warning "No log directories found on the device."
    exit 0
}

Write-Host "Found log directories:"
$foundPaths | ForEach-Object { Write-Host " - $_" }

# Collect file counts
$totalFiles = 0
$pathsToClean = @()

foreach ($path in $foundPaths) {
    # Check for files (csv, txt, log)
    $checkCmd = "$adbCmd shell ls $path"
    $files = Invoke-Expression $checkCmd
    
    if ($files -and $files -notmatch "No such file") {
        $fileList = $files -split "`r`n" | Where-Object { $_ -match "\.(csv|txt|log)$" }
        $count = $fileList.Count
        
        if ($count -gt 0) {
            Write-Host "  $path : $count log files found."
            $totalFiles += $count
            $pathsToClean += $path
        } else {
            Write-Host "  $path : Empty."
        }
    }
}

if ($totalFiles -eq 0) {
    Write-Host "No log files found to clean."
    exit 0
}

if (-not $Force) {
    $confirmation = Read-Host "Are you sure you want to delete ALL $totalFiles log files? (y/n)"
    if ($confirmation -ne 'y') {
        Write-Host "Operation cancelled."
        exit 0
    }
}

Write-Host "Deleting files..."

foreach ($path in $pathsToClean) {
    Write-Host "Cleaning $path ..."
    # Delete all files in the directory
    $deleteCmd = "$adbCmd shell rm -f $path/*"
    Invoke-Expression $deleteCmd
}

Write-Host "Cleanup complete."
