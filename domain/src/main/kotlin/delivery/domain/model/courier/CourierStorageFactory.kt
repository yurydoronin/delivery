package delivery.domain.model.courier

object CourierStorageFactory {

    fun create(
        type: CourierType
    ): List<StoragePlaceType> =
        when (type) {

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
}
