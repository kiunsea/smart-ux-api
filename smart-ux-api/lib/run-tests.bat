@echo off
REM Smart UX API 통합 테스트 실행 스크립트 (Windows)
REM 
REM 사용법:
REM   run-tests.bat          - 모든 테스트 실행
REM   run-tests.bat --info   - 상세 로그와 함께 실행
REM   run-tests.bat --clean  - 빌드 정리 후 실행

setlocal

set GRADLEW=..\..\gradlew.bat
set TEST_TASK=test

if "%1"=="--clean" (
    echo [INFO] 빌드 정리 중...
    call %GRADLEW% clean
    shift
)

if "%1"=="--info" (
    echo [INFO] 상세 로그 모드로 테스트 실행 중...
    call %GRADLEW% %TEST_TASK% --info
) else (
    echo [INFO] 테스트 실행 중...
    call %GRADLEW% %TEST_TASK%
)

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo 테스트 실행 완료!
    echo ========================================
    echo HTML 리포트: build\reports\tests\test\index.html
    echo ========================================
    echo.
    start build\reports\tests\test\index.html
) else (
    echo.
    echo ========================================
    echo 테스트 실패!
    echo ========================================
    echo 상세 정보는 위의 로그를 확인하세요.
    echo HTML 리포트: build\reports\tests\test\index.html
    echo ========================================
    echo.
    exit /b 1
)

endlocal

