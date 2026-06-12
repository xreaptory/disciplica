# Launches the Disciplica desktop app on Windows with NOTHING pre-installed.
#
# It resolves a Java 17 runtime in this order:
#   1. a portable JDK already bundled under .tools\ (created by build-jar.ps1),
#   2. otherwise it runs build-jar.ps1, which downloads a portable JDK 17 into .tools\
#      (no system install) and builds the runnable JAR,
#   3. as a last resort, a system-installed `java` on PATH.
# Then it starts the app from the runnable consumer JAR in client\target\.
#
# Fully portable: build once on any PC with internet, then copy this whole project
# folder (it now contains .tools\ and client\target\*.jar) to any other Windows PC
# and double-click run-app.bat -- it runs offline, with no Java install required.

$ErrorActionPreference = 'Stop'
$RepoRoot = $PSScriptRoot
$ToolsDir = Join-Path $RepoRoot '.tools'

function Find-BundledJava {
    if (Test-Path $ToolsDir) {
        $j = Get-ChildItem -Path $ToolsDir -Recurse -Filter 'java.exe' -File -ErrorAction SilentlyContinue |
             Where-Object { $_.DirectoryName -match '\\bin$' } | Select-Object -First 1
        if ($j) { return $j.FullName }
    }
    return $null
}

function Find-Jar {
    return Get-ChildItem (Join-Path $RepoRoot 'client\target') -Filter '*consumer.jar' -ErrorAction SilentlyContinue |
           Select-Object -First 1
}

Write-Host "=== Disciplica: launching the app ===" -ForegroundColor Cyan

$jar  = Find-Jar
$java = Find-BundledJava

# If either the JAR or a bundled Java is missing, run the build script: it both fetches
# a portable JDK 17 into .tools\ and produces the JAR. Safe to call repeatedly.
if (-not $jar -or -not $java) {
    Write-Host "First run: preparing Java and building the app (this can take a few minutes)..." -ForegroundColor Yellow
    & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $RepoRoot 'build-jar.ps1')
    if ($LASTEXITCODE -ne 0) { throw "Build step failed (exit $LASTEXITCODE); cannot launch." }
    $jar  = Find-Jar
    $java = Find-BundledJava
}

# Last resort: a system-installed java on PATH.
if (-not $java) {
    $sys = Get-Command java -ErrorAction SilentlyContinue
    if ($sys) { $java = $sys.Source }
}

if (-not $jar)  { throw "No runnable JAR found under client\target (the build may have failed)." }
if (-not $java) { throw "No Java runtime available, and the portable JDK download did not succeed." }

Write-Host "Java: $java"
Write-Host "App : $($jar.FullName)"
Write-Host "Starting Disciplica -- the application window will open..." -ForegroundColor Green

# Run from the project root so the app's local data\ folder is created here.
Set-Location -LiteralPath $RepoRoot
& $java '-jar' $jar.FullName
