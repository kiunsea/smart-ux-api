@echo off
setlocal enabledelayedexpansion

REM ============================================================
REM smart-ux-api 빌드 및 DoriBox에 JAR 배포 스크립트
REM 사용법:
REM   deploy_to_doribox.bat [DORIBOX_ROOT_PATH]
REM 기본 경로: D:\GIT\doribox
REM ============================================================

chcp 65001 >nul

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%~dp0.."
for %%I in ("%PROJECT_ROOT%") do set "PROJECT_ROOT=%%~fI"

if "%~1"=="" (
    set "DORIBOX_ROOT=D:\GIT\doribox"
) else (
    set "DORIBOX_ROOT=%~1"
)
set "DORIBOX_LIBS=%DORIBOX_ROOT%\\libs"

echo ============================================================
echo  smart-ux-api 빌드 및 DoriBox 배포
echo ------------------------------------------------------------
echo  smart-ux-api 경로 : %PROJECT_ROOT%
echo  DoriBox libs 경로 : %DORIBOX_LIBS%
echo ============================================================
echo.

if not exist "%DORIBOX_ROOT%" (
    echo [오류] DoriBox 프로젝트 경로를 찾을 수 없습니다: %DORIBOX_ROOT%
    echo        경로를 확인하거나 인자로 전달해주세요.
    exit /b 1
)

pushd "%PROJECT_ROOT%"

echo [1/3] Gradle 빌드를 수행합니다...
call gradlew.bat clean :lib:jar
if errorlevel 1 (
    echo [오류] smart-ux-api 빌드에 실패했습니다.
    popd
    exit /b 1
)

set "LIB_JAR_DIR=%PROJECT_ROOT%\\lib\\build\\libs"

echo [2/3] 최신 JAR 파일을 찾는 중...
set "LATEST_JAR="
if not exist "%LIB_JAR_DIR%" (
    echo [오류] JAR 디렉토리를 찾을 수 없습니다: %LIB_JAR_DIR%
    popd
    exit /b 1
)

for /f "delims=" %%F in ('dir /b /a:-d /o:-d "%LIB_JAR_DIR%\\smart-ux-api-*.jar"') do (
    set "LATEST_JAR=%%F"
    goto :FoundJar
)

:FoundJar
if not defined LATEST_JAR (
    echo [오류] %LIB_JAR_DIR% 에 smart-ux-api-*.jar 파일을 찾을 수 없습니다.
    popd
    exit /b 1
)

echo     -> 발견된 JAR: %LATEST_JAR%

if not exist "%DORIBOX_LIBS%" (
    echo [정보] DoriBox libs 폴더가 없어 새로 생성합니다.
    mkdir "%DORIBOX_LIBS%"
)

echo [3/3] JAR 파일을 복사합니다...
copy /Y "%LIB_JAR_DIR%\\%LATEST_JAR%" "%DORIBOX_LIBS%" >nul
if errorlevel 1 (
    echo [오류] JAR 복사에 실패했습니다.
    popd
    exit /b 1
)

popd

echo.
echo ✅ 배포 완료!
echo     %LATEST_JAR% -> %DORIBOX_LIBS%
echo.
echo 이제 DoriBox 프로젝트에서 libs 내부 JAR을 의존성으로 사용할 수 있습니다.
echo (build.gradle의 fileTree 설정이 되어 있는지 확인하세요.)

exit /b 0

