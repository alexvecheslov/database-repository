#!/bin/bash

# Останавливаем и удаляем существующие контейнеры
docker compose down

# Запускаем контейнеры
docker compose up -d

# Ждем, пока база данных будет готова
echo "Waiting for PostgreSQL to be ready..."
timeout=60
counter=0
until docker compose exec -T postgres pg_isready -U postgres -d nbank > /dev/null 2>&1; do
    sleep 1
    counter=$((counter + 1))
    if [ $counter -ge $timeout ]; then
        echo "PostgreSQL failed to start within $timeout seconds"
        exit 1
    fi
done

echo "PostgreSQL is ready!"

# Ждем, пока backend будет готов
echo "Waiting for backend to be ready..."
timeout=120
counter=0
until curl -f http://localhost:4111/actuator/health > /dev/null 2>&1; do
    sleep 2
    counter=$((counter + 2))
    if [ $counter -ge $timeout ]; then
        echo "Backend failed to start within $timeout seconds"
        docker compose logs backend
        exit 1
    fi
done

echo "Backend is ready!"
echo "Services are running:"
docker compose ps
