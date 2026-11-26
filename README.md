# Level-Up Gamer · Backend de la evaluación final

Continúa la tienda GameHub de la EV3. En Windows, extrae en una carpeta nueva y abre **INICIAR_BACKEND.cmd**, junto a este archivo. Instala JDK 21 y permite la descarga inicial de dependencias Maven. En Linux usa **bash scripts/iniciar-demo.sh**. No mezcles datos o el archivo de configuración de la EV3 con la demo final.

Consulta **http://localhost:8080/api/v1/productos** antes de abrir React. El modo demo usa H2 en disco; Docker Compose utiliza MySQL. En una demo nueva los correos de ejemplo son **ana.levelup@duoc.cl** (ADMIN), **luis.levelup@duoc.cl** (VENDEDOR) y **camila.levelup@duoc.cl** (CLIENTE). La clave se genera en el archivo local .env, variable CLAVE_DEMO. No subas ese archivo.

**Verificar:** Maven Wrapper con el objetivo verify, luego, con la demo local iniciada, python scripts/verificar-eft.py. Esta última comprobación inserta consultas y una compra de prueba; ejecútala sólo contra una base de demo. Los contratos se exploran en http://localhost:8080/swagger-ui.html.


Desarrollo Fullstack II · DSY1104  
Equipo: Brayan Alarcón, Benjamín Bravo y Daniela Salinas.

Backend Java 21 con Spring Boot. Continúa el GameHub Store de la EP2. Los datos del catálogo son los nueve productos del equipo, con sus fotos, precios y tres ofertas iniciales. Los pagos son simulados.

## Ejecutar con MySQL y Docker

1. Abra Docker Desktop. Prepare los valores locales en PowerShell: `powershell -NoProfile -File .\scripts\preparar-demo.ps1`. En macOS o Linux: `bash scripts/preparar-demo.sh`. Se genera un archivo `.env` que no se sube a Git.
2. Desde esta carpeta, ejecute `docker compose build eureka-server` y luego `docker compose up -d`. La compilación produce una sola imagen Java reutilizada por los servicios.
3. Espere a que `docker compose ps` muestre los servicios listos y abra [Swagger](http://localhost:8080/swagger-ui.html). Pruebe [el catálogo JSON](http://localhost:8080/api/v1/productos).
4. Ejecute el frontend en otra terminal. El navegador se comunica con el Gateway en el puerto 8080. Solo ese puerto se publica en loopback.
5. `docker compose down` detiene el entorno y conserva el volumen `mysql-data`. **No use `-v` si desea conservar los datos.**

MySQL crea los 11 esquemas `gh_*` mediante `database/01-esquemas.sh`. El perfil `mysql` valida el servidor con TLS por defecto. Compose desactiva TLS únicamente en la red privada de esta demo local. Fuera de Compose hay que preparar los esquemas, el usuario, TLS y secretos propios. MySQL no queda expuesto a la red del host.

## Alternativa local para la defensa sin Docker

Instale JDK 21 (la herramienta `javac -version` debe mostrar 21). Con internet para la primera compilación, ejecute:

- Windows PowerShell: `powershell -NoProfile -File .\scripts\iniciar-demo.ps1`
- macOS/Linux: `bash scripts/iniciar-demo.sh`

Esta variante conserva **la misma API, los 11 microservicios, Eureka y el Gateway**. Usa H2 relacional en disco, con archivos separados dentro de `datos/`. Mantenga la terminal abierta; Ctrl+C detiene los procesos. Los archivos y la orden permanecen. Consulte `.runtime/` si algún servicio no arranca. Al mostrar la demo, indique qué perfil de base de datos está activo.

## Cuentas de demostración

La contraseña se genera en la clave `CLAVE_DEMO` del archivo local `.env` cuando se prepara la demo; no hay contraseña escrita en el código de ejecución. La carga de cuentas ocurre solo al inicializar una base vacía con `DATOS_INICIALES=true`.

| Correo de prueba | Rol y alcance |
| --- | --- |
| ana.levelup@duoc.cl | ADMIN: mantenimiento comercial y operación de compras |
| luis.levelup@duoc.cl | VENDEDOR: lista y detalle de productos y órdenes |
| camila.levelup@duoc.cl | CLIENTE: compra, consulta sus órdenes y usa posventa |
| pedro.levelup@duoc.cl | CLIENTE desactivado: el login devuelve 403 |

Si conserva una base ya iniciada, conserve también su `.env`: generar otro archivo no cambia las contraseñas almacenadas. No utilice cuentas de demostración en un despliegue público.

## API y reglas

- Swagger reúne los 11 contratos en `/swagger-ui.html`. Las rutas de negocio están versionadas en `/api/v1`. Las rutas `/internal` se reservan a los microservicios.
- `auth-service` verifica contraseñas con BCrypt; el frontend envía el JWT como `Authorization: Bearer`. Los microservicios validan la firma, la expiración y el rol.
- `order-service` consulta precios, inventario y promociones; registra el resultado del pago. GAME10 descuenta solo las líneas sin oferta. El stock baja tras un pago aprobado, no tras uno rechazado ni al crear una transferencia pendiente.
- `shipping-service` registra el despacho generado por administración solo para una orden pagada. `review-service` valida compra previa, y `warranty-service` comprueba entrega y plazo.
- La ruta histórica `/api/v1/operador/ordenes` conserva el nombre técnico del ejemplo. VENDEDOR solo tiene GET de lista/detalle; las mutaciones y demás operaciones son ADMIN.
- API Gateway resuelve destinos con Eureka. OpenFeign comunica servicios. Cada servicio separa `controller`, `service`, `repository`, `model`, `dto` y configuración.

## Pruebas

En Windows: `mvnw.cmd verify` desde CMD, o `.\mvnw.cmd verify` en PowerShell. En macOS/Linux: `bash ./mvnw verify`. El perfil de pruebas usa H2 en memoria y dobles de los servicios vecinos. Los reportes quedan en `<servicio>/target/surefire-reports` y `<servicio>/target/site/jacoco`.

Las comprobaciones de integración de la entrega se ejecutaron por el Gateway con el perfil persistente `demo`. MySQL está configurado para Docker; no se reporta una ejecución de Compose como verificada.

## Referencias

Base técnica del docente: [backend GameHub de cacc92](https://github.com/cacc92/Duoc-Fullstack2-GameHubStore-BackEnd), revisión `73184e3`. Adaptaciones de la EP3: catálogo del equipo, versión de rutas, permisos de vendedor, persistencia configurable, scripts locales y regla GAME10. Consulte el ERS y los anexos de integración para los contratos y el alcance.

### Cambio de rol
Un administrador activo puede usar el panel de Usuarios para enviar PATCH /api/v1/admin/usuarios/{id}/rol con uno de estos valores: {"rol":"VENDEDOR"}, {"rol":"CLIENTE"} o {"rol":"ADMIN"}. Se modifica solo el rol. No se permite cambiar el rol propio. El registro público siempre crea clientes. Al editar roles o estados, el servicio bloquea ambas cuentas durante la operación y verifica el permiso actual del administrador en la base. Los otros servicios siguen validando el rol contenido en el JWT hasta que expire. La próxima sesión o renovación obtiene el rol actualizado.
