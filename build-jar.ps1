# Builds the runnable Disciplica consumer/fat JAR on Windows with zero prior setup.
#
# What it does, fully self-contained -- no Java, Maven, or IDE needs to be installed:
#   1. Ensures a JDK 17 is available. If none is found, it downloads a portable
#      Eclipse Temurin JDK 17 into .tools\ next to this script (nothing is installed
#      system-wide, nothing touches your PATH or registry).
#   2. Runs the project's Maven Wrapper (mvnw.cmd), which itself downloads the correct
#      Maven version on first use, and builds the client module (+ its shared dependency)
#      with tests skipped.
#   3. Verifies that client\target\*consumer.jar was produced and prints its path.
#
# Just double-click build-jar.bat, or run:  powershell -ExecutionPolicy Bypass -File build-jar.ps1

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'   # makes Invoke-WebRequest downloads fast

$RepoRoot  = $PSScriptRoot
$ToolsDir  = Join-Path $RepoRoot '.tools'
$JdkFeature = '17'

function Test-IsJdk17 {
    param([string]$JavaHome)
    if (-not $JavaHome) { return $false }
    $javaExe = Join-Path $JavaHome 'bin\java.exe'
    if (-not (Test-Path $javaExe)) { return $false }
    # `java -version` writes to STDERR. Lower the error preference locally so that
    # merging native stderr with 2>&1 does NOT become a terminating error under the
    # script-wide $ErrorActionPreference = 'Stop'.
    $ErrorActionPreference = 'SilentlyContinue'
    $out = & $javaExe '-version' 2>&1 | Out-String
    return ($out -match 'version "17' -or $out -match '"17\.')
}

function Get-JavaHome {
    # 1) Respect an already-good JAVA_HOME.
    if (Test-IsJdk17 $env:JAVA_HOME) {
        Write-Host "Using existing JDK 17 at JAVA_HOME: $env:JAVA_HOME"
        return $env:JAVA_HOME
    }

    # 2) Reuse a JDK this script downloaded on a previous run. We trust anything we
    #    extracted under .tools (it was a JDK 17 build), so just look for its java.exe.
    if (Test-Path $ToolsDir) {
        $cachedJava = Get-ChildItem -Path $ToolsDir -Recurse -Filter 'java.exe' -File -ErrorAction SilentlyContinue |
                      Where-Object { $_.DirectoryName -match '\\bin$' } | Select-Object -First 1
        if ($cachedJava) {
            $cachedHome = Split-Path -Parent $cachedJava.DirectoryName
            Write-Host "Using cached portable JDK 17 at: $cachedHome"
            return $cachedHome
        }
    }

    # 3) Download a portable Temurin JDK 17.
    $arch = if ($env:PROCESSOR_ARCHITECTURE -eq 'ARM64') { 'aarch64' } else { 'x64' }
    $url  = "https://api.adoptium.net/v3/binary/latest/$JdkFeature/ga/windows/$arch/jdk/hotspot/normal/eclipse?project=jdk"
    $zip  = Join-Path $ToolsDir "temurin-$JdkFeature-$arch.zip"

    Write-Host "No JDK 17 found. Downloading a portable Temurin JDK 17 ($arch)..."
    New-Item -ItemType Directory -Force -Path $ToolsDir | Out-Null
    Invoke-WebRequest -Uri $url -OutFile $zip
    Write-Host "Extracting JDK..."
    Expand-Archive -Path $zip -DestinationPath $ToolsDir -Force
    Remove-Item $zip -Force

    # Locate java.exe regardless of the exact extracted folder name; we downloaded a
    # JDK 17 build, so its presence is sufficient (no need to re-run java -version here).
    $javaExe = Get-ChildItem -Path $ToolsDir -Recurse -Filter 'java.exe' -File -ErrorAction SilentlyContinue |
               Where-Object { $_.DirectoryName -match '\\bin$' } | Select-Object -First 1
    if (-not $javaExe) {
        throw "JDK download/extract completed but no java.exe was found under $ToolsDir (the download may have been blocked or corrupted)."
    }
    $jdkHome = Split-Path -Parent $javaExe.DirectoryName
    Write-Host "Portable JDK 17 ready at: $jdkHome"
    return $jdkHome
}

# --- Build -------------------------------------------------------------------

Write-Host "=== Disciplica: building runnable consumer JAR ===" -ForegroundColor Cyan

$javaHome = Get-JavaHome
$env:JAVA_HOME = $javaHome
$env:PATH = (Join-Path $javaHome 'bin') + ';' + $env:PATH

$mvnw = Join-Path $RepoRoot 'mvnw.cmd'
if (-not (Test-Path $mvnw)) { throw "Maven Wrapper not found at $mvnw" }

Write-Host "Running the Maven Wrapper (downloads Maven on first run, skips tests)..." -ForegroundColor Cyan
& $mvnw '-B' '-ntp' '-pl' 'client' '-am' '-DskipTests' 'package'
if ($LASTEXITCODE -ne 0) {
    throw "Maven build failed with exit code $LASTEXITCODE."
}

# --- Verify the JAR was produced --------------------------------------------

$targetDir = Join-Path $RepoRoot 'client\target'
$jars = @(Get-ChildItem -Path $targetDir -Filter '*consumer.jar' -ErrorAction SilentlyContinue)
if ($jars.Count -eq 0) {
    Write-Host "ERROR: No consumer JAR found in client\target\ -- the build did not produce *consumer.jar" -ForegroundColor Red
    if (Test-Path $targetDir) {
        Write-Host "Contents of client\target:"
        Get-ChildItem -Path $targetDir | Format-Table -AutoSize | Out-String | Write-Host
    } else {
        Write-Host "client\target does not exist."
    }
    exit 1
}

$jar = $jars[0]
Write-Host ""
Write-Host "=== BUILD SUCCEEDED ===" -ForegroundColor Green
Write-Host "Runnable JAR: $($jar.FullName)"
$javaExe = Join-Path $javaHome 'bin\java.exe'
Write-Host ('Run it with:  "{0}" -jar "{1}"' -f $javaExe, $jar.FullName)
