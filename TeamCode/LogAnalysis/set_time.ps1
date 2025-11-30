# Script to sync PC time to Control Hub
# Usage: .\set_time.ps1

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

$currentDate = Get-Date -Format "MMddHHmmyyyy.ss"
Write-Host "Syncing Control Hub time to: $currentDate"

# The date format is MMddHHmmyyyy.ss
if ($adbSerial) {
    adb -s $adbSerial shell "date $currentDate"
} else {
    adb shell "date $currentDate"
}

Write-Host "Time sync complete."
