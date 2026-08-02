package delivery.domain.model.courier

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
