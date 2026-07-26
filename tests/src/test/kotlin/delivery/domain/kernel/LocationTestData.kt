package delivery.domain.kernel

object LocationTestData {

    fun random() =
        Location.restore(
            x = (1..10).random(),
            y = (1..10).random()
        )
}
