package delivery.application.ports.output.atomic

enum class TransactionPropagation {
    REQUIRED,
    REQUIRES_NEW,
}