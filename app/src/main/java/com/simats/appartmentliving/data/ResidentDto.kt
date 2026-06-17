package com.simats.appartmentliving.data

import com.google.gson.annotations.SerializedName
import com.simats.appartmentliving.ui.screens.AdminUser

data class ResidentDto(
    @SerializedName("_id") val id: String? = null,
    val residentName: String,
    val email: String,
    val phone: String,
    val block: String,
    val flatNumber: String,
    val flatType: String,
    val ownerType: String,
    val maintenanceAmount: Int,
    val status: String = "Pending",
    val createdAt: String? = null,
    val residentId: String? = null
) {
    fun toAdminUser(): AdminUser {
        val intId = kotlin.math.abs((id ?: "").hashCode())
        val initials = residentName.split(" ")
            .filter { it.isNotEmpty() }
            .map { it.first().uppercase() }
            .joinToString("")
            .take(2)
        
        return AdminUser(
            id = intId,
            name = residentName,
            flat = flatNumber,
            status = when (status.lowercase()) {
                "approved" -> "Approved"
                "rejected" -> "Rejected"
                "pending" -> "Pending"
                else -> status.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            },
            date = if (createdAt != null && createdAt.length >= 10) createdAt.substring(0, 10) else "Today",
            email = email,
            phone = phone,
            avatar = if (initials.isNotEmpty()) initials else "U"
        )
    }
}
