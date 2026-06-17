package com.simats.appartmentliving.data

import com.google.gson.annotations.SerializedName

data class PaymentDto(
    @SerializedName("_id") val id: String? = null,
    val residentName: String,
    val residentId: String,
    val flatNumber: String,
    val month: String,
    val paymentType: String? = "Maintenance",
    val upiId: String? = "",
    var paymentApp: String? = "",
    val qrCode: String? = "",
    val bankName: String? = "",
    val accountNumber: String? = "",
    val ifscCode: String? = "",
    var transactionId: String? = "",
    val paymentProof: String? = "",
    val approvalStatus: String? = "Waiting Verification",
    var status: String = "Unpaid",
    val maintenance: Int = 0,
    val waterBill: Int = 0,
    val electricityBill: Int = 0,
    val rent: Int = 0,
    val parkingFee: Int = 0,
    val penalty: Int = 0,
    val otherCharges: Int = 0,
    val totalAmount: Int = 0,
    val amount: Int? = null,
    val year: String? = null,
    val generatedDate: String? = null,
    val createdAt: String? = null
)
