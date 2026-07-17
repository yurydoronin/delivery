package delivery.domain.model.order

import delivery.common.types.base.ValueObject

data class Address(
    val country: String,
    val city: String,
    val street: String,
    val house: Int,
    val apartment: Int?,
) : ValueObject {

    companion object {
        fun of(
            country: String,
            city: String,
            street: String,
            house: Int,
            apartment: Int?,
        ): Address {
            require(country.isNotBlank()) { "Country must not be blank" }
            require(city.isNotBlank()) { "City must not be blank" }
            require(street.isNotBlank()) { "Street must not be blank" }

            require(house > 0) { "House number must be positive" }
            require(apartment == null || apartment > 0) {
                "Apartment number must be positive"
            }

            return Address(
                country = country.trim(),
                city = city.trim(),
                street = street.trim(),
                house = house,
                apartment = apartment,
            )
        }
    }
}
