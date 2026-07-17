package delivery.application.ports.output

interface UnitOfWork {
    fun commit()
}