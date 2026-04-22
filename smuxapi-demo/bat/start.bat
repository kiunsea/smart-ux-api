@echo off
REM ============================================================
REM  smart-ux-api — smuxapi-demo Quick Start
REM ------------------------------------------------------------
REM  Location: smuxapi-demo/bat/start.bat
REM  Usage   : double-click this file, or run from cmd.
REM  Requires: Java 17+ installed on PATH
REM  Effect  : Gradle build + Spring Boot bootRun (port 9090)
REM ============================================================
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1

REM Repo root is two levels up from this script (smuxapi-demo/bat → repo root).
set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..\..") do set "REPO_ROOT=%%~fI"
cd /d "%REPO_ROOT%"

set "GRADLEW=smart-ux-api\gradlew.bat"
if not exist "%GRADLEW%" (
    echo [ERROR] gradlew.bat not found: %REPO_ROOT%\%GRADLEW%
    echo Expected repo root: %REPO_ROOT%
    echo Run this bat from within the smart-ux-api repository.
    pause
    exit /b 1
)

if not exist "smuxapi-demo\build.gradle.kts" (
    echo [ERROR] smuxapi-demo project not found at: %REPO_ROOT%\smuxapi-demo
    pause
    exit /b 1
)

where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [WARN] java command not found on PATH.
    echo        Gradle toolchain may fetch JDK automatically, but local install is recommended.
    echo        Install Java 17+ from https://adoptium.net/ if startup fails.
    echo.
)

echo ========================================
echo   Smart UX API - Demo Quick Start
echo ========================================
echo   Repo root:    %REPO_ROOT%
echo   Port:         9090 (configurable in smuxapi-demo.yml)
echo   URL:          http://localhost:9090/smuxapi/
echo ========================================
echo.
echo Press Ctrl+C to stop the server.
echo.

call "%GRADLEW%" -p smuxapi-demo bootRun

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Failed to start. Check Java 17+ and Gradle setup.
    pause
    exit /b %ERRORLEVEL%
)
