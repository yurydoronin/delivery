# Сервис доставки

### В рамках [курса Кирилла Ветчинкина (11.09.2025 - 23.10.2025)](https://microarch.ru/courses/ddd/languages/java) реализуется один из сервисов Интернет-магазина.

### Были реализованы тактические паттерны DDD: 

- Aggregate, 
- Entity, 
- Value Object, 
- Domain Service, 
- Domain Event

### [Архитектура системы](image.png)

### Стек технологий: 

- Kotlin, Arrow-kt
- Spring Boot, Spring JDBC
- Gradle
- Kafka
- gRPC
- Postgres
- Flyway
- Docker
- Debezium

#### Дополнительные решения
- Unit of Work паттерн
- Command-query separation (CQS) 
- Реализован паттерн Outbox для гарантии отправки событий (at least once) в Kafka, при помощи Debezium (CDC)
