#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash scripts/preparar-demo.sh
# .env lo genera el script de demostración, o puede revisarlo y editarlo el equipo.
set -a
source ./.env
set +a
export SPRING_PROFILES_ACTIVE=demo DATOS_INICIALES=true SERVER_ADDRESS=127.0.0.1
export JAVA_TOOL_OPTIONS="-XX:ActiveProcessorCount=2 -Xms48m -Xmx256m"
export EUREKA_URL=http://localhost:8761/eureka
mkdir -p .runtime datos
bash ./mvnw -B -DskipTests package

procesos=()
detener() {
    for pid in "${procesos[@]}"; do kill "$pid" 2>/dev/null || true; done
}
trap detener EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

lanzar() {
    local modulo=$1
    PORT="$2" java -jar "$modulo/target/$modulo.jar" > ".runtime/$modulo.log" 2>&1 &
    procesos+=("$!")
}

lanzar eureka-server 8761
for intento in $(seq 1 90); do
    if curl -fsS http://localhost:8761/actuator/health > /dev/null 2>&1; then break; fi
    if [[ "$intento" == 90 ]]; then echo 'Eureka no arrancó: revisa .runtime/eureka-server.log'; exit 1; fi
    sleep 1
done

puerto=8081
for area in auth user category product inventory promotion order payment shipping review warranty; do
    lanzar "$area-service" "$puerto"
    puerto=$((puerto + 1))
done
lanzar api-gateway "${GATEWAY_PORT:-8080}"
echo "Iniciando servicios. Logs: .runtime. Swagger: http://localhost:${GATEWAY_PORT:-8080}/swagger-ui.html"
echo 'Cuando /api/v1/productos responda, inicia el frontend. Ctrl+C detiene esta demo.'
wait -n "${procesos[@]}"
