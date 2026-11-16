package delivery.domain.model.courier

fun CourierType.create(): List<StoragePlaceType> =
    when (this) {
        CourierType.WALKING ->
            listOf(
                StoragePlaceType.BACKPACK
            )

        CourierType.BICYCLE ->
            listOf(
                StoragePlaceType.BICYCLE_BACKPACK,
                StoragePlaceType.BICYCLE_TRUNK
            )

        CourierType.CAR ->
            listOf(
                StoragePlaceType.CAR_BACKPACK,
                StoragePlaceType.CAR_TRUNK,
                StoragePlaceType.CAR_TRAILER
            )
    }

enum class CourierType {
    WALKING,
    BICYCLE,
    CAR,
}

enum class StoragePlaceType(
    val volume: Int
) {
    BACKPACK(10),
    BICYCLE_BACKPACK(10),
    BICYCLE_TRUNK(30),
    CAR_BACKPACK(10),
    CAR_TRUNK(50),
    CAR_TRAILER(100),
}
