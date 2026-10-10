$ErrorActionPreference = "Stop"

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "      NexDesk Enterprise IT Service Management Platform" -ForegroundColor Cyan
Write-Host "      Spring Boot 3 + Oracle Database + Spring Security + JWT" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host ""

Set-Location $PSScriptRoot

$mavenCmd = "mvn"
if (-not (Get-Command "mvn" -ErrorAction SilentlyContinue)) {
    $fallbackPaths = @(
        "$env:USERPROFILE\Downloads\apache-maven-3.9.16-bin\apache-maven-3.9.16\bin\mvn.cmd",
        "C:\Users\rajko\Downloads\apache-maven-3.9.16-bin\apache-maven-3.9.16\bin\mvn.cmd"
    )
    foreach ($path in $fallbackPaths) {
        if (Test-Path $path) {
            $mavenCmd = $path
            break
        }
    }
}

$env:MAVEN_OPTS = "-Djavax.net.ssl.trustStoreType=Windows-ROOT"

Write-Host "[INFO] Maven Command: $mavenCmd" -ForegroundColor Green
Write-Host "[INFO] Launching Spring Boot Backend on http://localhost:8080..." -ForegroundColor Green
Write-Host "[INFO] OpenAPI Swagger UI: http://localhost:8080/swagger-ui/index.html" -ForegroundColor Green
Write-Host ""

& $mavenCmd spring-boot:run
