package com.simats.appartmentliving.ui.viewmodels

import com.simats.appartmentliving.data.RetrofitClient
import com.simats.appartmentliving.data.GooglePlacesResponse
import com.simats.appartmentliving.data.GooglePlaceDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PlaceItem(
    val id: String,
    val name: String,
    val rating: Float,
    val type: String, // hospital, pharmacy, supermarket
    val lat: Double,
    val lng: Double,
    val distance: Double, // calculated in km
    val isOpen: Boolean
)

class MapsViewModel {
    private val PLACES_API_KEY = "AIzaSyBh0EilleKwXJwGdBjCTOkm10puHaXGB3w"
    private val DEFAULT_LAT = 13.0827
    private val DEFAULT_LNG = 80.2707

    private val _places = MutableStateFlow<List<PlaceItem>>(emptyList())
    val places: StateFlow<List<PlaceItem>> = _places

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchNearbyPlaces()
    }

    fun fetchNearbyPlaces() {
        _isLoading.value = true
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                val types = listOf("hospital", "pharmacy", "grocery_or_supermarket")
                val fetched = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val list = mutableListOf<PlaceItem>()
                    for (type in types) {
                        try {
                            val response = RetrofitClient.apiService.getNearbyPlaces(
                                query = type,
                                location = "$DEFAULT_LAT,$DEFAULT_LNG",
                                radius = 5000,
                                key = PLACES_API_KEY
                            )
                            response.results.forEach { place ->
                                val lat = place.geometry?.location?.lat ?: DEFAULT_LAT
                                val lng = place.geometry?.location?.lng ?: DEFAULT_LNG
                                val distance = calculateDistance(DEFAULT_LAT, DEFAULT_LNG, lat, lng)
                                
                                val placeType = when {
                                    place.types?.contains("hospital") == true -> "hospital"
                                    place.types?.contains("pharmacy") == true -> "pharmacy"
                                    place.types?.contains("grocery_or_supermarket") == true || place.types?.contains("supermarket") == true -> "supermarket"
                                    else -> type
                                }
                                
                                list.add(
                                    PlaceItem(
                                        id = place.placeId,
                                        name = place.name,
                                        rating = place.rating ?: 4.2f,
                                        type = placeType,
                                        lat = lat,
                                        lng = lng,
                                        distance = distance,
                                        isOpen = place.openingHours?.openNow ?: true
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MapsViewModel", "Error fetching $type", e)
                        }
                    }
                    list.distinctBy { it.id }.sortedBy { it.distance }
                }
                
                if (fetched.isEmpty()) {
                    _places.value = getFallbackPlaces()
                } else {
                    _places.value = fetched
                }
                _error.value = null
            } catch (e: Exception) {
                _places.value = getFallbackPlaces()
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta))
        dist = Math.acos(dist)
        dist = Math.toDegrees(dist)
        dist = dist * 60 * 1.1515 * 1.609344 // Convert to kilometers
        return if (dist.isNaN()) 0.0 else dist
    }

    private fun getFallbackPlaces(): List<PlaceItem> {
        return listOf(
            PlaceItem("h1", "Apollo Hospital", 4.5f, "hospital", 13.0602, 80.2464, 1.2, true),
            PlaceItem("h2", "Fortis Malar Hospital", 4.3f, "hospital", 13.0063, 80.2575, 3.4, true),
            PlaceItem("h3", "MIOT International Hospital", 4.4f, "hospital", 13.0223, 80.1797, 5.8, true),
            PlaceItem("p1", "Apollo Pharmacy", 4.1f, "pharmacy", 13.0827, 80.2707, 0.4, true),
            PlaceItem("p2", "MedPlus Pharmacy", 4.2f, "pharmacy", 13.0850, 80.2680, 0.8, true),
            PlaceItem("s1", "Reliance Smart Superstore", 4.4f, "supermarket", 13.0900, 80.2800, 1.5, true),
            PlaceItem("s2", "Nilgiris Supermarket", 4.2f, "supermarket", 13.0780, 80.2600, 2.1, true),
            PlaceItem("s3", "Spar Hypermarket", 4.6f, "supermarket", 13.0400, 80.2500, 4.0, false)
        )
    }
}
