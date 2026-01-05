@echo off
REM smart-ux-api GitHub 릴리즈 생성 스크립트
REM 사용법: create-release.bat [버전]
REM 예: create-release.bat 0.6.0

setlocal

if "%1"=="" (
    echo 사용법: create-release.bat [버전]
    echo 예: create-release.bat 0.6.0
    exit /b 1
)

set VERSION=%1
set TAG_NAME=v%VERSION%
set JAR_FILE=smart-ux-api\lib\build\libs\smart-ux-api-%VERSION%.jar

echo ========================================
echo smart-ux-api 릴리즈 생성
echo ========================================
echo 버전: %VERSION%
echo 태그: %TAG_NAME%
echo JAR 파일: %JAR_FILE%
echo.

REM JAR 파일 확인
if not exist "%JAR_FILE%" (
    echo [오류] JAR 파일을 찾을 수 없습니다: %JAR_FILE%
    echo.
    echo JAR 파일을 먼저 빌드하세요:
    echo   cd smart-ux-api\lib
    echo   gradlew.bat build
    exit /b 1
)

echo [1/3] JAR 파일 확인 완료
echo.

REM GitHub CLI 확인
where gh >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [오류] GitHub CLI (gh)가 설치되어 있지 않습니다.
    echo.
    echo GitHub CLI 설치 방법:
    echo   1. https://cli.github.com/ 에서 다운로드
    echo   2. 또는: winget install GitHub.cli
    echo.
    echo GitHub CLI 없이 수동으로 릴리즈를 생성하려면:
    echo   1. https://github.com/kiunsea/smart-ux-api/releases/new 접속
    echo   2. Tag version: %TAG_NAME% 입력
    echo   3. Release title: %TAG_NAME% 입력
    echo   4. Release notes: CHANGELOG.md의 %VERSION% 섹션 내용 복사
    echo   5. %JAR_FILE% 파일을 드래그 앤 드롭
    echo   6. Publish release 클릭
    exit /b 1
)

echo [2/3] GitHub CLI 확인 완료
echo.

REM CHANGELOG에서 릴리즈 노트 추출
set RELEASE_NOTES=
for /f "tokens=*" %%a in ('findstr /C:"## [%VERSION%]" smart-ux-api\CHANGELOG.md') do (
    set RELEASE_NOTES=%%a
)

if "%RELEASE_NOTES%"=="" (
    echo [경고] CHANGELOG.md에서 %VERSION% 버전 정보를 찾을 수 없습니다.
    echo 기본 릴리즈 노트를 사용합니다.
    set RELEASE_NOTES=Release %TAG_NAME%
)

echo [3/3] 릴리즈 노트 준비 완료
echo.

REM GitHub CLI로 릴리즈 생성
echo GitHub에 릴리즈를 생성합니다...
echo.

gh release create %TAG_NAME% ^
    --title "%TAG_NAME%" ^
    --notes "%RELEASE_NOTES%" ^
    "%JAR_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ✅ 릴리즈 생성 완료!
    echo ========================================
    echo 릴리즈 URL: https://github.com/kiunsea/smart-ux-api/releases/tag/%TAG_NAME%
    echo.
    echo doribox CI가 자동으로 이 릴리즈를 감지하여 업데이트합니다.
) else (
    echo.
    echo ========================================
    echo ❌ 릴리즈 생성 실패
    echo ========================================
    echo 오류 코드: %ERRORLEVEL%
    echo.
    echo 수동으로 릴리즈를 생성하려면:
    echo   https://github.com/kiunsea/smart-ux-api/releases/new
)

endlocal

