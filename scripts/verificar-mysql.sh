#!/usr/bin/env bash
# Verificación aislada: base efímera de prueba y puerto local, no modifica la tienda ni su base.
set -euo pipefail
cd "$(dirname "$0")/.."
nombre="levelup-mysql-prueba-$RANDOM"
export DB_CLAVE="$(openssl rand -hex 20)" DB_USUARIO=prueba_eft
export DB_HOST=127.0.0.1 DB_PORT=33316 DB_SSL_MODE=DISABLED DB_PUBLIC_KEY=true
export JWT_SECRETO="$(openssl rand -hex 32)" CLAVE_INTERNA="$(openssl rand -hex 32)"
export DATOS_INICIALES=false SPRING_PROFILES_ACTIVE=mysql EUREKA_CLIENT_ENABLED=false SERVER_ADDRESS=127.0.0.1 PORT=18082
jar=user-service/target/user-service.jar
[[ -f "$jar" ]] || bash mvnw -B -ntp -pl user-service -am -DskipTests package
docker run --name "$nombre" -d -p "127.0.0.1:$DB_PORT:3306" -e MYSQL_RANDOM_ROOT_PASSWORD=yes -e MYSQL_DATABASE=gh_user -e MYSQL_USER="$DB_USUARIO" -e MYSQL_PASSWORD="$DB_CLAVE" mysql:8.4 >/dev/null
pid=''
limpiar() { if [[ -n "$pid" ]]; then kill "$pid" 2>/dev/null || true; fi; docker rm -f "$nombre" >/dev/null 2>&1 || true; }
trap limpiar EXIT
sql() { docker exec -e MYSQL_PWD="$DB_CLAVE" "$nombre" mysql -N -s -u"$DB_USUARIO" gh_user -e "$1"; }
for i in {1..90}; do if sql 'SELECT 1' >/dev/null 2>&1; then break; fi; sleep 2; done
sql 'SELECT VERSION()' | awk '{print "Motor MySQL:", $1}'
java -jar "$jar" > mysql-ci-servicio.log 2>&1 & pid=$!
for i in {1..90}; do if curl -fsS http://127.0.0.1:18082/actuator/health >/dev/null 2>&1; then break; fi; sleep 1; done
salida=$(curl -fsS -H 'Content-Type: application/json' -d '{"nombre":"Demo EFT","comentario":"Persistencia MySQL"}' http://127.0.0.1:18082/api/v1/contacto)
id=$(python3 -c 'import sys,json;print(int(json.load(sys.stdin)["id"]))' <<< "$salida")
[[ $(sql "SELECT COUNT(*) FROM consultas_contacto WHERE id=$id AND comentario='Persistencia MySQL'") == 1 ]]
kill "$pid"; wait "$pid" || true; pid=''
[[ $(sql "SELECT COUNT(*) FROM consultas_contacto WHERE id=$id") == 1 ]]
echo 'OK: Spring Boot recibió el POST, JPA guardó la fila y MySQL la conserva al detener Java.'
