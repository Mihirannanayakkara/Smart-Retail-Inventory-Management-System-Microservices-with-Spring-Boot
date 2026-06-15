$MVN = "C:\Program Files\JetBrains\IntelliJ IDEA 2024.3.1.1\plugins\maven\lib\maven3\bin\mvn.cmd"
$BASE = "D:\thamindu\smart-retail-system"

New-Item -ItemType Directory -Path "$BASE\logs" -Force | Out-Null

$services = @(
    @{ name = "eureka-server";            port = 8761; dir = "eureka-server" },
    @{ name = "api-gateway";              port = 9090; dir = "api-gateway" },
    @{ name = "user-service";             port = 9091; dir = "user-service" },
    @{ name = "product-service";          port = 9092; dir = "product-service" },
    @{ name = "order-service";            port = 9093; dir = "order-service" },
    @{ name = "supplier-restock-service"; port = 9094; dir = "supplier-restock-service" },
    @{ name = "notification-service";     port = 9095; dir = "notification-service" }
)

function Wait-ForPort($port, $timeoutSec = 90) {
    $deadline = (Get-Date).AddSeconds($timeoutSec)
    while ((Get-Date) -lt $deadline) {
        try {
            $tcp = New-Object System.Net.Sockets.TcpClient
            $tcp.Connect("localhost", $port)
            $tcp.Close()
            return $true
        } catch { Start-Sleep 2 }
    }
    return $false
}

foreach ($svc in $services) {
    $dir     = "$BASE\$($svc.dir)"
    $logFile = "$BASE\logs\$($svc.name).log"
    $title   = $svc.name

    Write-Host "Starting $($svc.name) ..." -ForegroundColor Cyan

    # Launch each service in its own CMD window (survives parent script exit)
    $args = "/k title $title && cd /d `"$dir`" && `"$MVN`" spring-boot:run > `"$logFile`" 2>&1"
    Start-Process "cmd.exe" -ArgumentList $args -WindowStyle Minimized

    Write-Host "  Waiting for port $($svc.port)..." -ForegroundColor Yellow
    $ready = Wait-ForPort $svc.port 90
    if ($ready) {
        Write-Host "  [UP] $($svc.name) :$($svc.port)" -ForegroundColor Green
    } else {
        Write-Host "  [TIMEOUT] $($svc.name) port $($svc.port) - continuing" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host " ALL SERVICES RUNNING" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host " Eureka Dashboard:    http://localhost:8761"
Write-Host " API Gateway:         http://localhost:9090"
Write-Host " RabbitMQ UI:         http://localhost:15672  (guest/guest)"
Write-Host " Swagger - User:      http://localhost:9091/swagger-ui.html"
Write-Host " Swagger - Product:   http://localhost:9092/swagger-ui.html"
Write-Host " Swagger - Order:     http://localhost:9093/swagger-ui.html"
Write-Host " Swagger - Supplier:  http://localhost:9094/swagger-ui.html"
Write-Host " Swagger - Notif:     http://localhost:9095/swagger-ui.html"
Write-Host " Swagger - Master:    http://localhost:9090/swagger-ui.html"
Write-Host "============================================" -ForegroundColor Green
