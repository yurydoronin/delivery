# ADR-001. Отказ от Hibernate в пользу Spring JDBC

## STATUS

Accepted

## CONTEXT

Проект построен по принципам **Clean Architecture** с разделением ответственности между слоями.

Доменная модель не должна знать:

- о Spring;
- о Hibernate / JPA;
- о способе хранения данных;
- об инфраструктурных механизмах persistence.

Поэтому используются две разные модели:

```text
Infrastructure                    Domain

ORM Entity  ──toDomain()──>  Domain Entity
                                  │
                                  │ изменения в use case
                                  ▼
ORM Entity  <──toEntity()──  Domain Entity
```

Hibernate предполагает другую модель работы: объектная сущность находится в `Session` и управляется `Persistence Context` через identity map, dirty checking и flush.

В нашем use case домен работает только с `Domain Entity`:

```kotlin
val order = orderRepository.findAnyCreated()
    ?: raise(OrderNotFound)

val couriers = courierRepository.getAvailableCouriers()
val courier = orderDispatcher.dispatch(order, couriers).bind()

orderRepository.track(order)
courierRepository.track(courier)

unitOfWork.commit()
```

При чтении из БД Hibernate загружает ORM Entity в `Session`, после чего она преобразуется в доменную сущность. В `Unit of Work` изменённый доменный агрегат снова преобразуется в ORM Entity:

```text
1. OrderEntity(id=1) загружается Hibernate
2. OrderEntity(id=1) -> Order
3. Use Case изменяет Order
4. Unit of Work перекладывает Order -> в OrderEntity(id=1)
5. save(OrderEntity)
```

В одной `Session` в результате оказываются два разных ORM-объекта с одним идентификатором:

```text
Session
├── OrderEntity(id=1)  ← загружен из БД
└── OrderEntity(id=1)  ← создан из Domain Entity
```

Hibernate не допускает, чтобы в одном persistence context два разных экземпляра представляли одну и ту же сущность с одинаковым типом и идентификатором и выбрасывает ошибку:

```text
A different object with the same identifier value
was already associated with the session
```

## DECISION

Отказаться от Hibernate и использовать **Spring JDBC (`JdbcClient`)** для доступа к PostgreSQL.

Причина решения — необходимость сохранить полное разделение Domain и Infrastructure и не связывать `Unit of Work` с моделью `Persistence Context` Hibernate.

Используем следующую модель:

```text
Domain
  │
  ▼
Repository Port
  │
  ▼
Infrastructure
  │
  ▼
Spring JDBC / JdbcClient
  │
  ▼
PostgreSQL
```

Сохранение агрегата выполняется через явные SQL-операции:

```text
Domain Aggregate
       ↓
Persistence Model / параметры SQL
       ↓
INSERT / UPDATE / DELETE
       ↓
PostgreSQL
```

`Unit of Work` управляет транзакцией и группирует изменения нескольких агрегатов в одну атомарную операцию, не завися от состояния Hibernate `Session`.

## WHY NOT HIBERNATE

Hibernate хорошо работает, когда Domain Entity и ORM Entity являются одним объектом:

```text
Domain Entity == @Entity

Session
└── Order(id=1)
```

В этом случае Hibernate может использовать свою основную модель:

```text
Entity
  ↓
Persistence Context
  ↓
Dirty Checking
  ↓
Flush
```

В нашем проекте это невозможно, поскольку домен должен оставаться независимым от ORM.

Технически проблему с двумя объектами можно обходить через `merge()`, но тогда `Unit of Work` начинает учитывать внутреннюю модель Hibernate: `Session`, managed/detached entity, `merge()` и `flush()`.

Это нарушает исходную цель разделения слоёв: инфраструктурный механизм хранения начинает влиять на код прикладного слоя.

Поэтому проблема заключается не в том, что Hibernate «плохой» или не поддерживает DDD. Проблема в том, что его модель работы плохо соответствует выбранной архитектуре:

```text
Strict Clean Architecture
+
отдельная Domain Entity
+
отдельная Persistence Entity
+
Unit of Work
```

## CONSEQUENCES

### Положительные

- Домен полностью независим от Hibernate и Spring.
- Domain Entity и Persistence Entity остаются разными моделями.
- `Unit of Work` не зависит от `Persistence Context` Hibernate.
- Нет зависимости от lifecycle `managed / detached / transient`.
- Не требуется `merge()` для синхронизации двух ORM-объектов.
- SQL и момент изменения данных контролируются приложением.
- Транзакция охватывает все изменения use case без зависимости от ORM lifecycle.

### Отрицательные

- SQL необходимо писать явно.
- Необходимо самостоятельно выполнять mapping между Persistence Model и Domain Model.
- Нет dirty checking.
- Часть работы, которую ORM выполняет автоматически, становится ответственностью persistence-слоя.

## NOTES

Подход 1: DDD-lite + Hibernate, ORM Entity одновременно является доменной сущностью, Domain = JPA Entity.

Подход 2: **строгое разделение слоёв и полная независимость домена от инфраструктуры**. Для строгой Clean Architecture Hibernate начинает сопротивляться.
```text
JPA Entity
↓
Domain
↓
JPA Entity
```
При таком подходе Spring JDBC лучше соответствует архитектуре, чем Hibernate.
