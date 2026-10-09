#!/usr/bin/env bash

set -Eeuo pipefail

APP_NAME="ysc-backend"
IMAGE_NAME="ysc-backend:latest"
NETWORK_NAME="ysc_default"
ENV_FILE=".env.prod"
UPLOAD_DIR="${HOME}/ysc-uploads"
UPLOAD_CONTAINER_PATH="/app/uploads"
MESSAGE_CACHE_URL="http://127.0.0.1:8080/api/msg/all"
MESSAGE_CACHE_RESPONSE=""

log() {
    local level="$1"
    shift
    printf '%s | %-5s | %s\n' "$(date '+%Y-%m-%d %H:%M:%S%z')" "$level" "$*"
}

on_error() {
    local exit_code=$?
    log ERROR "배포 실패 (line ${BASH_LINENO[0]:-unknown}, exit=${exit_code})"
    if docker container inspect "$APP_NAME" >/dev/null 2>&1; then
        log ERROR "실패한 컨테이너의 최근 로그"
        docker logs --tail 80 "$APP_NAME" 2>&1 || true
    fi
    exit "$exit_code"
}

cleanup() {
    if [[ -n "$MESSAGE_CACHE_RESPONSE" ]]; then
        rm -f "$MESSAGE_CACHE_RESPONSE"
    fi
}

trap on_error ERR
trap cleanup EXIT

log INFO "YSC Backend 배포 시작"
log INFO "대상 컨테이너=$APP_NAME, 이미지=$IMAGE_NAME, 네트워크=$NETWORK_NAME"

log INFO "[1/7] main 브랜치 최신 코드 가져오기"
git pull --ff-only origin main
DEPLOY_COMMIT="$(git rev-parse --short HEAD)"
log INFO "배포 커밋=$DEPLOY_COMMIT"

log INFO "[2/7] 배포 환경 확인"
if [[ ! -f "$ENV_FILE" ]]; then
    log ERROR "환경 파일을 찾을 수 없습니다: $ENV_FILE"
    exit 1
fi
if ! docker network inspect "$NETWORK_NAME" >/dev/null 2>&1; then
    log ERROR "Docker 네트워크를 찾을 수 없습니다: $NETWORK_NAME"
    exit 1
fi

log INFO "[3/7] Gradle 빌드 및 검증"
./gradlew clean build

log INFO "[4/7] Docker 이미지 빌드"
docker build --pull -t "$IMAGE_NAME" .

log INFO "[5/8] 업로드 파일 저장소 준비"
mkdir -p "$UPLOAD_DIR"
if [[ -z "$(find "$UPLOAD_DIR" -mindepth 1 -print -quit)" ]] \
    && docker container inspect "$APP_NAME" >/dev/null 2>&1 \
    && docker exec "$APP_NAME" test -d "$UPLOAD_CONTAINER_PATH"; then
    # 기존 컨테이너의 업로드를 영구 호스트 디렉터리로 옮겨 컨테이너 교체 후에도 보존합니다.
    docker cp "$APP_NAME:$UPLOAD_CONTAINER_PATH/." "$UPLOAD_DIR/"
    log INFO "기존 컨테이너의 업로드 파일을 영구 저장 경로로 복사했습니다."
fi

log INFO "[6/8] 기존 애플리케이션 컨테이너 교체"
if docker container inspect "$APP_NAME" >/dev/null 2>&1; then
    docker rm -f "$APP_NAME" >/dev/null
    log INFO "기존 컨테이너를 제거했습니다."
else
    log INFO "기존 컨테이너가 없어 신규 배포로 진행합니다."
fi

log INFO "[7/8] 새 애플리케이션 컨테이너 시작"
docker run -d \
    --name "$APP_NAME" \
    --network "$NETWORK_NAME" \
    --env-file "$ENV_FILE" \
    --env SPRING_PROFILES_ACTIVE=prod \
    --env "FILE_STORAGE_ROOT=$UPLOAD_CONTAINER_PATH" \
    --publish 127.0.0.1:8080:8080 \
    --volume "$UPLOAD_DIR:$UPLOAD_CONTAINER_PATH" \
    --restart unless-stopped \
    --label "com.ysc.deploy.commit=$DEPLOY_COMMIT" \
    "$IMAGE_NAME" >/dev/null

log INFO "애플리케이션 기동 및 메시지 캐시 초기화 확인"
MESSAGE_CACHE_RESPONSE="$(mktemp)"
READY=0
for attempt in $(seq 1 30); do
    http_status="$(curl --silent --show-error --max-time 5 \
        --output "$MESSAGE_CACHE_RESPONSE" \
        --write-out '%{http_code}' \
        "$MESSAGE_CACHE_URL" 2>/dev/null || true)"
    response_compact="$(tr -d '[:space:]' < "$MESSAGE_CACHE_RESPONSE" 2>/dev/null || true)"

    if [[ "$http_status" == "200" && -n "$response_compact" && "$response_compact" != "{}" ]]; then
        READY=1
        break
    fi

    log WARN "기동 대기 중 (${attempt}/30, HTTP=${http_status:-연결 실패})"
    sleep 2
done

if [[ "$READY" -ne 1 ]]; then
    log ERROR "애플리케이션 준비 확인 또는 메시지 캐시 적재에 실패했습니다."
    exit 1
fi

# /api/msg/all은 DB 메시지로 MSG:ALL 해시를 다시 구성합니다.
# Redis 전체 flush는 로그인 세션 등 다른 Redis 데이터를 지우므로 수행하지 않습니다.
if command -v jq >/dev/null 2>&1; then
    MESSAGE_COUNT="$(jq 'length' "$MESSAGE_CACHE_RESPONSE" 2>/dev/null || printf '0')"
    if [[ ! "$MESSAGE_COUNT" =~ ^[1-9][0-9]*$ ]]; then
        log ERROR "메시지 API에서 DB 메시지를 받지 못했습니다."
        exit 1
    fi
else
    MESSAGE_COUNT="확인 생략(jq 미설치)"
fi
log INFO "메시지 캐시를 DB 기준으로 갱신했습니다 (항목=${MESSAGE_COUNT})."

log INFO "[8/8] 배포 결과"
docker ps --filter "name=^/${APP_NAME}$" --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'
log INFO "배포 완료 (commit=$DEPLOY_COMMIT)"
log INFO "애플리케이션 최근 로그"
docker logs --tail 30 "$APP_NAME" 2>&1 || true
