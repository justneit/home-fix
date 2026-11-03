#!/usr/bin/env bash
# Creates .env from .env.example with freshly generated local secrets.
# Refuses to overwrite an existing .env.
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ -e .env ]]; then
  echo ".env already exists; delete it first if you want to regenerate it." >&2
  exit 1
fi

secret() { openssl rand -hex "$1"; }

sed -e "s|^DB_PASSWORD=.*|DB_PASSWORD=$(secret 16)|" \
    -e "s|^MYSQL_ROOT_PASSWORD=.*|MYSQL_ROOT_PASSWORD=$(secret 16)|" \
    -e "s|^MINIO_ROOT_PASSWORD=.*|MINIO_ROOT_PASSWORD=$(secret 16)|" \
    -e "s|^JWT_SECRET=.*|JWT_SECRET=$(secret 32)|" \
    .env.example > .env
chmod 600 .env
echo "Created .env with generated secrets. Review ports (e.g. DB_PORT if 3306 is taken)."
