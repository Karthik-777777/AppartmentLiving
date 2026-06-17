package com.simats.appartmentliving.data

import com.google.gson.annotations.SerializedName
import com.simats.appartmentliving.ui.screens.Complaint
import com.simats.appartmentliving.ui.screens.TimelineStep
import com.simats.appartmentliving.ui.screens.AdminComment
import com.simats.appartmentliving.ui.screens.AdminComplaint

data class ComplaintDto(
    @SerializedName("_id") val id: String? = null,
    val residentId: String,
    val residentName: String,
    val flatNumber: String,
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val status: String = "Pending",
    val createdAt: String? = null,
    val adminNote: String? = null,
    val photoAttached: Boolean = false
)

fun ComplaintDto.toDomainComplaint(): Complaint {
    val rawId = this.id ?: ""
    val dateStr = if (!this.createdAt.isNullOrBlank() && this.createdAt.contains("T")) {
        val datePart = this.createdAt.substringBefore("T") // e.g. "2026-05-26"
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val year = parts[0]
            val monthIdx = parts[1].toIntOrNull() ?: 1
            val day = parts[2]
            "${months.getOrNull(monthIdx) ?: "May"} $day, $year"
        } else {
            "May 22, 2026"
        }
    } else {
        "May 22, 2026"
    }
    
    // Timeline steps based on status
    val timelineSteps = when (this.status) {
        "Resolved" -> listOf(
            TimelineStep("Raised", dateStr, "done"),
            TimelineStep("Assigned", dateStr, "done"),
            TimelineStep("In Progress", dateStr, "done"),
            TimelineStep("Resolved", dateStr, "done")
        )
        "In Progress" -> listOf(
            TimelineStep("Raised", dateStr, "done"),
            TimelineStep("Assigned", dateStr, "done"),
            TimelineStep("In Progress", dateStr, "active"),
            TimelineStep("Resolved", "Pending", "pending")
        )
        else -> listOf(
            TimelineStep("Raised", dateStr, "active"),
            TimelineStep("Assigned", "Pending", "pending"),
            TimelineStep("In Progress", "Pending", "pending"),
            TimelineStep("Resolved", "Pending", "pending")
        )
    }

    val adminCommentObj = if (!this.adminNote.isNullOrBlank()) {
        AdminComment(
            name = "Suresh Kumar",
            initials = "SK",
            comment = this.adminNote,
            timeAgo = "Just now"
        )
    } else {
        null
    }

    return Complaint(
        id = rawId,
        title = this.title,
        category = this.category,
        date = dateStr,
        status = this.status,
        priority = this.priority,
        description = this.description,
        timeline = timelineSteps,
        adminComment = adminCommentObj,
        photoAttached = this.photoAttached
    )
}

fun ComplaintDto.toDomainAdminComplaint(): AdminComplaint {
    val rawId = this.id ?: ""
    // Hashing String ID to a unique stable Int for the UI
    val intId = kotlin.math.abs(rawId.hashCode())
    
    return AdminComplaint(
        id = intId,
        title = this.title,
        user = this.residentName,
        flat = this.flatNumber,
        category = this.category,
        status = this.status,
        priority = this.priority,
        description = this.description,
        phone = "+91 98765 54321", // default mock phone
        image = if (this.photoAttached) "photo.jpg" else "",
        adminNote = this.adminNote ?: ""
    )
}
