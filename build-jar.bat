@echo off
REM Double-click this to build the runnable Disciplica consumer JAR.
REM It needs nothing pre-installed (no Java, no Maven, no IDE) — build-jar.ps1
REM downloads a portable JDK 17 and uses the project's Maven Wrapper.
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-jar.ps1"
echo.
pause
