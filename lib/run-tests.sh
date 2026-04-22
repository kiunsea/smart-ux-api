#!/bin/bash
# Smart UX API 통합 테스트 실행 스크립트 (Linux/Mac)
# 
# 사용법:
#   ./run-tests.sh          - 모든 테스트 실행
#   ./run-tests.sh --info   - 상세 로그와 함께 실행
#   ./run-tests.sh --clean  - 빌드 정리 후 실행

set -e

GRADLEW="../../gradlew"
TEST_TASK="test"

if [ "$1" == "--clean" ]; then
    echo "[INFO] 빌드 정리 중..."
    $GRADLEW clean
    shift
fi

if [ "$1" == "--info" ]; then
    echo "[INFO] 상세 로그 모드로 테스트 실행 중..."
    $GRADLEW $TEST_TASK --info
else
    echo "[INFO] 테스트 실행 중..."
    $GRADLEW $TEST_TASK
fi

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "테스트 실행 완료!"
    echo "========================================"
    echo "HTML 리포트: build/reports/tests/test/index.html"
    echo "========================================"
    echo ""
    
    # Linux/Mac에서 브라우저 열기 (선택적)
    if command -v xdg-open &> /dev/null; then
        xdg-open build/reports/tests/test/index.html
    elif command -v open &> /dev/null; then
        open build/reports/tests/test/index.html
    fi
else
    echo ""
    echo "========================================"
    echo "테스트 실패!"
    echo "========================================"
    echo "상세 정보는 위의 로그를 확인하세요."
    echo "HTML 리포트: build/reports/tests/test/index.html"
    echo "========================================"
    echo ""
    exit 1
fi

