#!/usr/bin/env bash
set -euo pipefail

APP_DIR="${APP_DIR:-$(pwd)}"
BRANCH="${BRANCH:-main}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.hostinger.yml}"
ENV_FILE="${ENV_FILE:-.env.hostinger}"

cd "$APP_DIR"

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is not installed on this host."
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "Docker Compose plugin is not available."
  exit 1
fi

if [ ! -f "$ENV_FILE" ]; then
  if [ -f ".env.hostinger.example" ]; then
    cp .env.hostinger.example "$ENV_FILE"
    echo "Created $ENV_FILE from template. Update the secrets before deploying."
  else
    echo "Missing $ENV_FILE and .env.hostinger.example."
  fi
  exit 1
fi

if [ -z "${FRONTEND_CONTEXT:-}" ]; then
  while IFS='=' read -r key value; do
    if [ "$key" = "FRONTEND_CONTEXT" ]; then
      FRONTEND_CONTEXT="$value"
      break
    fi
  done < "$ENV_FILE"
fi
FRONTEND_CONTEXT="${FRONTEND_CONTEXT:-../KSentinel-Web}"

if [ -d ".git" ]; then
  echo "Updating source from branch $BRANCH..."
  git fetch origin "$BRANCH"
  git checkout "$BRANCH"
  git pull --ff-only origin "$BRANCH"
fi

if [ ! -f "$FRONTEND_CONTEXT/package.json" ]; then
  echo "Frontend project not found at $FRONTEND_CONTEXT."
  echo "Clone KSentinel-Web next to this project or set FRONTEND_CONTEXT in $ENV_FILE."
  exit 1
fi

if [ -d "$FRONTEND_CONTEXT/.git" ]; then
  echo "Updating frontend source from branch $BRANCH..."
  (
    cd "$FRONTEND_CONTEXT"
    git fetch origin "$BRANCH"
    git checkout "$BRANCH"
    git pull --ff-only origin "$BRANCH"
  )
fi

echo "Building and starting KSentinel..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --build

echo "Waiting for containers..."
sleep 5

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps

echo
echo "Deployment finished."
echo "App logs:"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs app --tail 50
echo
echo "Web logs:"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs web --tail 50
