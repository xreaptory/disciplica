@echo off
REM Double-click to run the Disciplica app. Nothing needs to be installed:
REM run-app.ps1 uses (or downloads) a portable Java 17 and builds the app if needed.
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-app.ps1"
echo.
pause
