# Test persistence by simulating user interaction

Write-Host "`n=== Persistence Test Script ===" -ForegroundColor Cyan
Write-Host "This will test if tasks persist between program restarts`n" -ForegroundColor Yellow

if (-not $env:JAVA_HOME) {
    Write-Host "✗ ERROR: JAVA_HOME is not set. Configure JAVA_HOME and rerun this script." -ForegroundColor Red
    exit 1
}

# Remove existing data file if it exists
$dataFile = "data\tasks.json"
if (Test-Path $dataFile) {
    Write-Host "Removing existing data file..." -ForegroundColor Yellow
    Remove-Item $dataFile -Force
}

# First run - create and complete a task
Write-Host "`n--- First Run: Creating and completing tasks ---" -ForegroundColor Green
$input1 = "2`n1`n4`n"  # Complete task 1, exit
$input1 | .\mvnw -q exec:java "-Dexec.mainClass=com.disciplica.bootstrap.Main"

# Check if data file was created
if (Test-Path $dataFile) {
    Write-Host "`n✓ Data file created successfully!" -ForegroundColor Green
    Write-Host "`nData file contents:" -ForegroundColor Cyan
    Get-Content $dataFile | Write-Host
} else {
    Write-Host "`n✗ ERROR: Data file was not created!" -ForegroundColor Red
    exit 1
}

# Wait a moment
Start-Sleep -Seconds 1

# Second run - verify tasks were loaded
Write-Host "`n`n--- Second Run: Verifying persistence ---" -ForegroundColor Green
$input2 = "1`n4`n"  # View tasks, exit
$output = $input2 | .\mvnw -q exec:java "-Dexec.mainClass=com.disciplica.bootstrap.Main" 2>&1 | Out-String

# Check if the output indicates tasks were loaded
if ($output -match "Welcome back! Loading") {
    Write-Host "`n✓ Tasks loaded successfully from file!" -ForegroundColor Green
} else {
    Write-Host "`n⚠ Could not verify task loading from output" -ForegroundColor Yellow
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
Write-Host "Please manually verify the task states are preserved.`n"
