data class NearbySearchResponse(
    val results: List<Place>,
    val status: String,
    val errorMessage: String? = null
)

data class Place(
    val name: String,
    val vicinity: String,
    val geometry: Geometry,
    val place_id: String
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)