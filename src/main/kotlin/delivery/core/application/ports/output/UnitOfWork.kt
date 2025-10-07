package delivery.core.application.ports.output

interface UnitOfWork {
    fun commit()
}