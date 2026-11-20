# ADR-002. Отказ от `@Transactional` в пользу явного управления транзакцией для операций записи

## STATUS

Accepted

## CONTEXT

В проекте используется функциональная обработка ошибок через `Either` из Arrow. Ожидаемые бизнес-ошибки возвращаются как `Either.Left`, а не выбрасываются как исключения.

Spring `@Transactional` по умолчанию откатывает транзакцию при исключении `RuntimeException` или `Error`. Возврат `Either.Left` для Spring — обычное завершение метода, поэтому транзакция может зафиксироваться, даже если бизнес-операция завершилась ошибкой.

Это опасно, когда до возврата ошибки данные в БД уже были изменены: часть изменений может сохраниться, хотя операция в целом не удалась.

## DECISION

Для операций *записи*, которые изменяют состояние БД и возвращают типизированные ошибки через `Either`, не использовать `@Transactional`.

Такие операции выполнять через `AtomicOperationPort`. Его реализация — `AtomicOperationAdapter` — использует `TransactionTemplate` из Spring для управления транзакцией и определяет её исход по результату операции:

```text
Either.Right → commit
Either.Left  → rollback
```

При `Either.Left` адаптер помечает транзакцию для отката и возвращает исходную бизнес-ошибку без преобразования в исключение. Прикладной слой (`:application`) зависит от `AtomicOperationPort`, а не от Spring.

Операции *чтения* не требуют использования `AtomicOperationPort`.

## WHY NOT `@Transactional`

`@Transactional` не анализирует значение `Either`. Для него `Either.Left` — не ошибка транзакции, а обычный результат выполнения метода. Чтобы гарантировать откат при типизированной бизнес-ошибке, требуется явно связать результат операции с завершением транзакции.

`AtomicOperationPort` задаёт эту связь явно и сохраняет функциональную обработку ошибок без исключений для ожидаемых бизнес-ситуаций.

## CONSEQUENCES

### Положительные

- `Either.Left` приводит к откату транзакции.
- Типизированная бизнес-ошибка возвращается вызывающему коду без преобразования в исключение.
- Прикладной слой не зависит от Spring.
- Поддерживаются режимы распространения транзакции `REQUIRED` и `REQUIRES_NEW`.
- При выбрасывании `RuntimeException` или `Error` транзакция откатывается по правилам Spring.
- Граница транзакции и правило отката выражены явно.

### Отрицательные

- Операции записи, которым нужна атомарность, должны выполняться через `AtomicOperationPort`.
- Порт и адаптер необходимо поддерживать и тестировать.

## NOTES

- Контракт: [`AtomicOperationPort.kt`](https://github.com/yurydoronin/delivery/blob/main/application/src/main/kotlin/delivery/application/ports/output/atomic/AtomicOperationPort.kt).
- Реализация: [`AtomicOperationAdapter.kt`](https://github.com/yurydoronin/delivery/blob/main/infrastructure/src/main/kotlin/delivery/infrastructure/output/adapters/AtomicOperationAdapter.kt).
- Правила отката Spring: [Rolling Back a Declarative Transaction](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/rolling-back.html).
