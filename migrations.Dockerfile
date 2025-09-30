FROM docker.io/flyway/flyway:latest

# Копируем только SQL миграции
COPY src/main/resources/db/migration /flyway/sql
WORKDIR /flyway

# Запуск миграций
CMD ["migrate", "-X"]
