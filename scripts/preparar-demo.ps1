$ErrorActionPreference = 'Stop'
Set-Location (Split-Path -Parent $PSScriptRoot)
function Nuevo-Hex([int]$Cantidad) {
    $bytes = New-Object byte[] $Cantidad
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    return ([BitConverter]::ToString($bytes)).Replace('-', '').ToLowerInvariant()
}
if (-not (Test-Path .env)) {
    @(
        "DB_CLAVE=$(Nuevo-Hex 20)"
        "MYSQL_ROOT_PASSWORD=$(Nuevo-Hex 20)"
        "JWT_SECRETO=$(Nuevo-Hex 32)"
        "CLAVE_INTERNA=$(Nuevo-Hex 32)"
        "CLAVE_DEMO=Gh1$(Nuevo-Hex 8)"
        'CORS_ORIGENES=http://localhost:5173,http://127.0.0.1:5173'
        'GATEWAY_PORT=8080'
    ) | Set-Content .env -Encoding ascii
}
Write-Output 'Configuracion local disponible. CLAVE_DEMO esta en .env (no compartir).'
