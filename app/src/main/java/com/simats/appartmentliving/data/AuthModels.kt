package com.simats.appartmentliving.data

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String,
    val residentName: String,
    val phone: String,
    val block: String,
    val flatNumber: String,
    val flatType: String,
    val ownerType: String,
    val maintenanceAmount: Int
)

data class AuthResponse(
    val token: String,
    val role: String,
    val user: UserDto
)

data class UserDto(
    @SerializedName("_id") val id: String,
    val email: String,
    val role: String,
    val residentName: String? = null,
    val residentId: String? = null,
    val flatNumber: String? = null,
    val block: String? = null,
    val flatType: String? = null,
    val ownerType: String? = null,
    val maintenanceAmount: Int? = null,
    val status: String? = null,
    val phone: String? = null
)

data class SendOtpRequest(
    val email: String
)

data class SendOtpResponse(
    val message: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class VerifyOtpResponse(
    val verified: Boolean,
    val message: String
)

data class GooglePlacesResponse(
    val results: List<GooglePlaceDto>
)

data class GooglePlaceDto(
    @SerializedName("place_id") val placeId: String,
    val name: String,
    val rating: Float? = null,
    val types: List<String>? = null,
    val geometry: GoogleGeometryDto? = null,
    @SerializedName("opening_hours") val openingHours: GoogleOpeningHoursDto? = null
)

data class GoogleGeometryDto(
    val location: GoogleLatLngDto
)

data class GoogleLatLngDto(
    val lat: Double,
    val lng: Double
)

data class GoogleOpeningHoursDto(
    @SerializedName("open_now") val openNow: Boolean? = null
)

