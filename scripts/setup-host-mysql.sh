#!/usr/bin/env bash
# Creates the HomeFix database and application user on a MySQL server installed on this machine
# (instead of the docker compose "mysql" service). Values come from .env.
# Requires admin access to MySQL: by default it runs `sudo mysql` (Ubuntu auth_socket root).
# Override with MYSQL_ADMIN_CMD, e.g. MYSQL_ADMIN_CMD="mysql -uroot -p".
# Safe to re-run: it creates missing objects and resets the user's password to DB_PASSWORD.
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ ! -f .env ]]; then
  echo ".env not found; run ./scripts/init-env.sh first." >&2
  exit 1
fi

env_value() { grep -E "^$1=" .env | tail -n1 | cut -d= -f2-; }
DB_NAME=$(env_value DB_NAME)
DB_USERNAME=$(env_value DB_USERNAME)
DB_PASSWORD=$(env_value DB_PASSWORD)

for v in DB_NAME DB_USERNAME DB_PASSWORD; do
  if [[ -z "${!v}" ]]; then
    echo "$v is empty in .env" >&2
    exit 1
  fi
done
if [[ ! "$DB_NAME" =~ ^[A-Za-z0-9_]+$ || ! "$DB_USERNAME" =~ ^[A-Za-z0-9_]+$ ]]; then
  echo "DB_NAME and DB_USERNAME may only contain letters, digits and underscores." >&2
  exit 1
fi
if [[ "$DB_PASSWORD" == *"'"* || "$DB_PASSWORD" == *"\\"* ]]; then
  echo "DB_PASSWORD must not contain quotes or backslashes." >&2
  exit 1
fi

read -r -a ADMIN <<< "${MYSQL_ADMIN_CMD:-sudo mysql}"

"${ADMIN[@]}" <<SQL
CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS '$DB_USERNAME'@'localhost' IDENTIFIED BY '$DB_PASSWORD';
CREATE USER IF NOT EXISTS '$DB_USERNAME'@'127.0.0.1' IDENTIFIED BY '$DB_PASSWORD';
ALTER USER '$DB_USERNAME'@'localhost' IDENTIFIED BY '$DB_PASSWORD';
ALTER USER '$DB_USERNAME'@'127.0.0.1' IDENTIFIED BY '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$DB_USERNAME'@'localhost';
GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$DB_USERNAME'@'127.0.0.1';
FLUSH PRIVILEGES;
SQL

echo "Database '$DB_NAME' and user '$DB_USERNAME' are ready on the host MySQL."
echo "Make sure .env has DB_HOST=localhost and the host MySQL port (usually DB_PORT=3306)."
