#!/bin/bash

set -e

APP_NAME="ysc-backend"
IMAGE_NAME="ysc-backend:latest"
NETWORK_NAME="ysc_default"
ENV_FILE=".env.prod"

echo "======================================"
echo " YSC Backend Deploy Start"
echo "======================================"

echo "[1/6] Git Pull"
git pull origin main

echo "[2/6] Check Environment"
if [ ! -f "$ENV_FILE" ]; then
    echo "ERROR: $ENV_FILE not found."
    exit 1
fi

if ! docker network inspect "$NETWORK_NAME" > /dev/null 2>&1; then
    echo "ERROR: Docker network $NETWORK_NAME not found."
    exit 1
fi

echo "[3/6] Gradle Build"
./gradlew clean build

echo "[4/6] Docker Image Build"
docker build -t "$IMAGE_NAME" .

echo "[5/6] Stop Old Container"
docker rm -f "$APP_NAME" 2>/dev/null || true

echo "[6/6] Start New Container"
docker run -d \
  --name "$APP_NAME" \
  --network "$NETWORK_NAME" \
  --env-file "$ENV_FILE" \
  -e SPRING_PROFILES_ACTIVE=prod \
  -p 127.0.0.1:8080:8080 \
  --restart unless-stopped \
  "$IMAGE_NAME"

echo ""
echo "======================================"
echo " YSC Backend Deploy Complete"
echo "======================================"

echo ""
echo "Container Status:"
docker ps --filter "name=$APP_NAME"

echo ""
echo "Recent Logs:"
docker logs --tail 30 "$APP_NAME"
