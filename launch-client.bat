@echo off
setlocal EnableExtensions EnableDelayedExpansion

cd /d "%~dp0"

set "PROPERTIES_FILE=client\src\main\resources\disciplica-client.properties"
set "MAIN_CLASS=com.disciplica.client.ClientApplication"

if not exist "%PROPERTIES_FILE%" (
    echo [ERROR] Missing %PROPERTIES_FILE%.
    echo Run this script from the Disciplica repository root.
    exit /b 1
)

set "API_BASE_URL="
for /f "usebackq tokens=1,* delims==" %%A in ("%PROPERTIES_FILE%") do (
    if /i "%%A"=="apiBaseUrl" set "API_BASE_URL=%%B"
)

if not defined API_BASE_URL (
    echo [ERROR] %PROPERTIES_FILE% does not define apiBaseUrl.
    exit /b 1
)

echo %API_BASE_URL% | findstr /i /c:"localhost" /c:"127.0.0.1" /c:"0.0.0.0" >nul
if not errorlevel 1 (
    echo [ERROR] apiBaseUrl still points to a local server:
    echo         %API_BASE_URL%
    echo Update %PROPERTIES_FILE% to your live Render backend URL, for example:
    echo         apiBaseUrl=https://disciplica-api.onrender.com
    exit /b 1
)

echo %API_BASE_URL% | findstr /r /i "^https://.*" >nul
if errorlevel 1 (
    echo [WARNING] apiBaseUrl is not HTTPS:
    echo           %API_BASE_URL%
    echo Render production backends should normally use https://.
)

echo [OK] Using backend: %API_BASE_URL%
echo [OK] Compiling client and required modules...
call .\mvnw.cmd -q -am -pl client -DskipTests compile
if errorlevel 1 (
    echo [ERROR] Client compilation failed.
    exit /b 1
)

echo [OK] Launching Disciplica client...
call .\mvnw.cmd -am -pl client exec:java -Dexec.mainClass="%MAIN_CLASS%"
exit /b %ERRORLEVEL%
