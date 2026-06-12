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
    # Lower the error preference for the whole function: a stray JAVA_HOME and
    # `java -version` writing to STDERR must never become terminating errors under
    # the script-wide $ErrorActionPreference = 'Stop'.
    $ErrorActionPreference = 'SilentlyContinue'
    if ([string]::IsNullOrWhiteSpace($JavaHome)) { return $false }
    # Guard against a malformed JAVA_HOME containing characters illegal in paths
    # (e.g. '<', '>'), which would make Test-Path/Join-Path throw.
    if ($JavaHome.IndexOfAny([System.IO.Path]::GetInvalidPathChars()) -ge 0) { return $false }
    $javaExe = Join-Path $JavaHome 'bin\java.exe'
    if (-not (Test-Path -LiteralPath $javaExe)) { return $false }
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

$wrapperJar = Join-Path $RepoRoot '.mvn\wrapper\maven-wrapper.jar'
if (-not (Test-Path $wrapperJar)) {
    throw "Maven Wrapper jar not found at $wrapperJar (is this the full project folder?)."
}
$javaExe = Join-Path $javaHome 'bin\java.exe'

Write-Host "Running the Maven Wrapper (downloads Maven on first run, skips tests)..." -ForegroundColor Cyan
# Build the project that lives next to THIS script, regardless of the directory the
# script was launched from. Maven resolves which pom.xml to build from the current
# working directory, so pin it to the project root.
Set-Location -LiteralPath $RepoRoot
# Invoke the wrapper's Java launcher directly instead of mvnw.cmd. The .cmd batch
# script breaks when the project path contains parentheses -- e.g. a browser-numbered
# "disciplica-main (3)" download folder -- because cmd.exe mis-parses the parens.
# Calling java straight from PowerShell sidesteps cmd.exe entirely; Java handles such
# paths fine. This mirrors exactly what mvnw.cmd does internally.
& $javaExe '-classpath' $wrapperJar "-Dmaven.multiModuleProjectDirectory=$RepoRoot" `
    'org.apache.maven.wrapper.MavenWrapperMain' `
    '-B' '-ntp' '-pl' 'client' '-am' '-DskipTests' 'package'
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
