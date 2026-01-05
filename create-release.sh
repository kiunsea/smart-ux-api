#!/bin/bash
# smart-ux-api GitHub 릴리즈 생성 스크립트
# 사용법: ./create-release.sh [버전]
# 예: ./create-release.sh 0.6.0

set -e

if [ -z "$1" ]; then
    echo "사용법: ./create-release.sh [버전]"
    echo "예: ./create-release.sh 0.6.0"
    exit 1
fi

VERSION="$1"
TAG_NAME="v${VERSION}"
JAR_FILE="smart-ux-api/lib/build/libs/smart-ux-api-${VERSION}.jar"

echo "========================================"
echo "smart-ux-api 릴리즈 생성"
echo "========================================"
echo "버전: ${VERSION}"
echo "태그: ${TAG_NAME}"
echo "JAR 파일: ${JAR_FILE}"
echo ""

# JAR 파일 확인
if [ ! -f "${JAR_FILE}" ]; then
    echo "[오류] JAR 파일을 찾을 수 없습니다: ${JAR_FILE}"
    echo ""
    echo "JAR 파일을 먼저 빌드하세요:"
    echo "  cd smart-ux-api/lib"
    echo "  ./gradlew build"
    exit 1
fi

echo "[1/3] JAR 파일 확인 완료"
echo ""

# GitHub CLI 확인
if ! command -v gh &> /dev/null; then
    echo "[오류] GitHub CLI (gh)가 설치되어 있지 않습니다."
    echo ""
    echo "GitHub CLI 설치 방법:"
    echo "  macOS: brew install gh"
    echo "  Linux: https://github.com/cli/cli/blob/trunk/docs/install_linux.md"
    echo "  Windows: https://cli.github.com/"
    echo ""
    echo "GitHub CLI 없이 수동으로 릴리즈를 생성하려면:"
    echo "  1. https://github.com/kiunsea/smart-ux-api/releases/new 접속"
    echo "  2. Tag version: ${TAG_NAME} 입력"
    echo "  3. Release title: ${TAG_NAME} 입력"
    echo "  4. Release notes: CHANGELOG.md의 ${VERSION} 섹션 내용 복사"
    echo "  5. ${JAR_FILE} 파일을 드래그 앤 드롭"
    echo "  6. Publish release 클릭"
    exit 1
fi

echo "[2/3] GitHub CLI 확인 완료"
echo ""

# CHANGELOG에서 릴리즈 노트 추출
RELEASE_NOTES=$(grep -A 50 "## \[${VERSION}\]" smart-ux-api/CHANGELOG.md | head -20 || echo "Release ${TAG_NAME}")

if [ -z "${RELEASE_NOTES}" ] || [ "${RELEASE_NOTES}" = "Release ${TAG_NAME}" ]; then
    echo "[경고] CHANGELOG.md에서 ${VERSION} 버전 정보를 찾을 수 없습니다."
    echo "기본 릴리즈 노트를 사용합니다."
    RELEASE_NOTES="Release ${TAG_NAME}"
fi

echo "[3/3] 릴리즈 노트 준비 완료"
echo ""

# GitHub CLI로 릴리즈 생성
echo "GitHub에 릴리즈를 생성합니다..."
echo ""

gh release create "${TAG_NAME}" \
    --title "${TAG_NAME}" \
    --notes "${RELEASE_NOTES}" \
    "${JAR_FILE}"

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "✅ 릴리즈 생성 완료!"
    echo "========================================"
    echo "릴리즈 URL: https://github.com/kiunsea/smart-ux-api/releases/tag/${TAG_NAME}"
    echo ""
    echo "doribox CI가 자동으로 이 릴리즈를 감지하여 업데이트합니다."
else
    echo ""
    echo "========================================"
    echo "❌ 릴리즈 생성 실패"
    echo "========================================"
    echo "수동으로 릴리즈를 생성하려면:"
    echo "  https://github.com/kiunsea/smart-ux-api/releases/new"
fi



