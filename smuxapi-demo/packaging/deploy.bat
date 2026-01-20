@echo off
REM smuxapi-demo 배포 스크립트
REM Gradle의 deploy task를 실행하여 배포 패키지를 생성합니다.

setlocal enabledelayedexpansion

REM 한글 출력을 위한 코드페이지 설정 (UTF-8)
chcp 65001 >nul 2>&1

REM 현재 스크립트의 디렉터리 경로 저장
set "SCRIPT_DIR=%~dp0"
set "PROJECT_DIR=%SCRIPT_DIR%.."

REM 프로젝트 디렉터리로 이동
cd /d "%PROJECT_DIR%"

REM Gradle Wrapper 확인
if exist "gradlew.bat" (
    set "GRADLEW=%CD%\gradlew.bat"
    echo [INFO] Gradle Wrapper를 사용합니다: %GRADLEW%
) else if exist "..\smart-ux-api\gradlew.bat" (
    REM 상위 디렉터리의 smart-ux-api의 gradlew.bat 사용 (경로만 사용)
    set "GRADLEW=%SCRIPT_DIR%..\..\smart-ux-api\gradlew.bat"
    echo [INFO] 상위 프로젝트의 Gradle Wrapper를 사용합니다: %GRADLEW%
) else (
    REM 시스템 Gradle 확인
    where gradle >nul 2>&1
    if %ERRORLEVEL% NEQ 0 (
        echo ========================================
        echo   오류: Gradle을 찾을 수 없습니다!
        echo ========================================
        echo.
        echo 다음 중 하나를 확인하세요:
        echo   1. smuxapi-demo 디렉터리에 gradlew.bat 파일이 있는지
        echo   2. 상위 디렉터리에 smart-ux-api/gradlew.bat이 있는지
        echo   3. 시스템에 Gradle이 설치되어 있는지
        echo.
        pause
        exit /b 1
    )
    set "GRADLEW=gradle"
    echo [INFO] 시스템 Gradle을 사용합니다.
)

REM 배포 프로세스 시작
echo ========================================
echo   smuxapi-demo 배포 시작
echo ========================================
echo.
echo 작업 디렉터리: %CD%
echo.

REM deploy task 실행 (smuxapi-demo 디렉터리에서 실행)
call "%GRADLEW%" deploy

REM 실행 결과 확인
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo   배포 중 오류가 발생했습니다!
    echo ========================================
    pause
    exit /b 1
)

echo.
echo ========================================
echo   배포가 완료되었습니다!
echo ========================================
echo.
echo 배포 파일 위치:
echo   packaging\distribution\smuxapi-demo.zip
echo.
pause
