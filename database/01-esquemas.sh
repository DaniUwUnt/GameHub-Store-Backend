#!/usr/bin/env bash
# Ejecutado una vez por la imagen oficial de MySQL al crear el volumen.
set -euo pipefail

for area in auth user category product inventory promotion order payment shipping review warranty; do
    printf 'CREATE DATABASE IF NOT EXISTS gh_%s CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci; GRANT ALL PRIVILEGES ON gh_%s.* TO \x27gamehub\x27@\x27%%\x27;\n' "$area" "$area"
done | mysql --protocol=socket -uroot -p"$MYSQL_ROOT_PASSWORD"
