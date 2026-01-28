# PowerShell script для запуска Docker Compose на Windows

Write-Host "Stopping existing containers..." -ForegroundColor Yellow
docker compose down

Write-Host "Starting containers..." -ForegroundColor Yellow
docker compose up -d

Write-Host "Waiting for PostgreSQL to be ready..." -ForegroundColor Yellow
$timeout = 60
$counter = 0
do {
    Start-Sleep -Seconds 1
    $counter++
    $result = docker compose exec -T postgres pg_isready -U postgres -d nbank 2>&1
    if ($LASTEXITCODE -eq 0) {
        break
    }
    if ($counter -ge $timeout) {
        Write-Host "PostgreSQL failed to start within $timeout seconds" -ForegroundColor Red
        exit 1
    }
} while ($true)

Write-Host "PostgreSQL is ready!" -ForegroundColor Green

Write-Host "Waiting for backend to be ready..." -ForegroundColor Yellow
$timeout = 120
$counter = 0
do {
    Start-Sleep -Seconds 2
    $counter += 2
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:4111/actuator/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            break
        }
    } catch {
        # Continue waiting
    }
    if ($counter -ge $timeout) {
        Write-Host "Backend failed to start within $timeout seconds" -ForegroundColor Red
        docker compose logs backend
        exit 1
    }
} while ($true)

Write-Host "Backend is ready!" -ForegroundColor Green
Write-Host "Services are running:" -ForegroundColor Green
docker compose ps
