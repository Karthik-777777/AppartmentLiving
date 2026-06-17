package com.simats.appartmentliving.data

import com.google.gson.annotations.SerializedName

data class NoticeDto(
    @SerializedName("_id") val id: String? = null,
    val title: String,
    val description: String,
    val category: String,
    val createdBy: String,
    val createdAt: String? = null,
    val priority: String
)
