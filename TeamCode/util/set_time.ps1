# Script to sync PC time to Control Hub
# Usage: .\set_time.ps1

$currentDate = Get-Date -Format "MMddHHmmyyyy.ss"
Write-Host "Syncing Control Hub time to: $currentDate"

# The date format is MMddHHmmyyyy.ss
adb shell "date $currentDate"

Write-Host "Time sync complete."
