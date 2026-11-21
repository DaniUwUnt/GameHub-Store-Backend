$ErrorActionPreference = 'Stop'
Set-Location (Split-Path -Parent $PSScriptRoot)
& "$PSScriptRoot\preparar-demo.ps1"
Get-Content .env | ForEach-Object {
    if ($_ -match '^([A-Z_]+)=(.*)$') {
        [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process')
    }
}
$env:SPRING_PROFILES_ACTIVE='demo'
$env:DATOS_INICIALES='true'
$env:SERVER_ADDRESS='127.0.0.1'
$env:EUREKA_URL='http://localhost:8761/eureka'
$env:JAVA_TOOL_OPTIONS='-XX:ActiveProcessorCount=2 -Xms48m -Xmx256m'
if (-not $env:GATEWAY_PORT) { $env:GATEWAY_PORT = '8080' }
New-Item -ItemType Directory -Force .runtime, datos | Out-Null
& .\mvnw.cmd -B -DskipTests package
if ($LASTEXITCODE -ne 0) { throw 'No compilo backend. Revisa que JAVA_HOME apunte al JDK 21.' }
$procesos = New-Object System.Collections.Generic.List[System.Diagnostics.Process]
$raiz = (Get-Location).Path
function Lanzar([string]$Modulo, [int]$Puerto) {
    $env:PORT = "$Puerto"
    $jar = Join-Path $raiz "$Modulo\target\$Modulo.jar"
    $p = Start-Process java -ArgumentList '-jar', ('"' + $jar + '"') -WorkingDirectory $raiz -PassThru -NoNewWindow `
        -RedirectStandardOutput ".runtime\$Modulo.log" -RedirectStandardError ".runtime\$Modulo.error.log"
    $procesos.Add($p)
}
try {
    Lanzar 'eureka-server' 8761
    for ($i=0; $i -lt 90; $i++) {
        try { Invoke-RestMethod 'http://localhost:8761/actuator/health' | Out-Null; break }
        catch { if ($i -eq 89) { throw 'Eureka no arranco. Revisa .runtime\eureka-server.log' }; Start-Sleep 1 }
    }
    $areas = 'auth','user','category','product','inventory','promotion','order','payment','shipping','review','warranty'
    for ($i=0; $i -lt $areas.Length; $i++) { Lanzar "$($areas[$i])-service" (8081 + $i) }
    Lanzar 'api-gateway' ([int]$env:GATEWAY_PORT)
    Write-Output "Iniciando servicios. Logs: .runtime. Swagger: http://localhost:$env:GATEWAY_PORT/swagger-ui.html"
    Write-Output 'Cuando /api/v1/productos responda, inicia el frontend. Ctrl+C detiene la demo.'
    while ($true) {
        foreach ($p in $procesos) { if ($p.HasExited) { throw 'Un servicio se detuvo. Revisa sus logs en .runtime.' } }
        Start-Sleep 2
    }
} finally {
    foreach ($p in $procesos) { if (-not $p.HasExited) { Stop-Process -Id $p.Id -ErrorAction SilentlyContinue } }
}
