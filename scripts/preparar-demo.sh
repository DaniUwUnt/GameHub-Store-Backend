#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
umask 077
if [[ ! -e .env ]]; then
    {
        printf 'DB_CLAVE=%s\n' "$(openssl rand -hex 20)"
        printf 'MYSQL_ROOT_PASSWORD=%s\n' "$(openssl rand -hex 20)"
        printf 'JWT_SECRETO=%s\n' "$(openssl rand -hex 32)"
        printf 'CLAVE_INTERNA=%s\n' "$(openssl rand -hex 32)"
        printf 'CLAVE_DEMO=Gh1%s\n' "$(openssl rand -hex 8)"
        printf 'CORS_ORIGENES=http://localhost:5173,http://127.0.0.1:5173\nGATEWAY_PORT=8080\n'
    } > .env
fi
echo 'Configuración local disponible. CLAVE_DEMO está en .env (no compartir).'
