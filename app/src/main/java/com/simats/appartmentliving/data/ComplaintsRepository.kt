package com.simats.appartmentliving.data

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.simats.appartmentliving.ui.screens.Complaint
import com.simats.appartmentliving.ui.screens.AdminComplaint
import com.simats.appartmentliving.ui.screens.AdminUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ComplaintsRepository(private val coroutineScope: CoroutineScope) {
    // Keep a static cache of the raw DTOs to look up string IDs
    companion object {
        var rawDtos = listOf<ComplaintDto>()
        var rawResidentDtos = listOf<ResidentDto>()
    }

    fun syncResidentComplaints(
        residentId: String,
        localList: SnapshotStateList<Complaint>,
        onStart: () -> Unit,
        onComplete: (Throwable?) -> Unit
    ) {
        coroutineScope.launch {
            onStart()
            try {
                val list = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getResidentComplaints(residentId)
                }
                rawDtos = list
                
                localList.clear()
                // Reverse list to show newest first
                list.reversed().map { it.toDomainComplaint() }.forEach { localList.add(it) }
                onComplete(null)
            } catch (e: Exception) {
                onComplete(e)
            }
        }
    }

    fun syncAdminComplaints(
        localList: SnapshotStateList<AdminComplaint>,
        onStart: () -> Unit,
        onComplete: (Throwable?) -> Unit
    ) {
        coroutineScope.launch {
            onStart()
            try {
                val list = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getAllComplaints()
                }
                rawDtos = list
                
                localList.clear()
                // Reverse list to show newest first
                list.reversed().map { it.toDomainAdminComplaint() }.forEach { localList.add(it) }
                onComplete(null)
            } catch (e: Exception) {
                onComplete(e)
            }
        }
    }

    fun addResidentComplaint(
        title: String,
        description: String,
        category: String,
        priority: String,
        photoAttached: Boolean,
        residentName: String,
        residentId: String,
        flatNumber: String,
        localList: SnapshotStateList<Complaint>,
        onComplete: (Throwable?) -> Unit
    ) {
        coroutineScope.launch {
            try {
                val newDto = ComplaintDto(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    photoAttached = photoAttached,
                    residentName = residentName,
                    residentId = residentId,
                    flatNumber = flatNumber,
                    status = "Pending"
                )
                val created = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.createComplaintNew(newDto)
                }
                // Add new complaint to the top of the local list
                localList.add(0, created.toDomainComplaint())
                onComplete(null)
            } catch (e: Exception) {
                onComplete(e)
            }
        }
    }

    fun updateComplaintStatusAndNote(
        complaintId: Int, // Hashed ID from UI
        status: String,
        adminNote: String,
        localAdminList: SnapshotStateList<AdminComplaint>,
        onComplete: (Throwable?) -> Unit
    ) {
        coroutineScope.launch {
            try {
                val matchedDto = rawDtos.find { kotlin.math.abs((it.id ?: "").hashCode()) == complaintId }
                val stringId = matchedDto?.id
                if (stringId != null) {
                    val updateBody = mapOf(
                        "status" to status,
                        "adminNote" to adminNote
                    )
                    val updated = withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.updateComplaintStatus(stringId, updateBody)
                    }
                    
                    val index = localAdminList.indexOfFirst { it.id == complaintId }
                    if (index != -1) {
                        localAdminList[index] = updated.toDomainAdminComplaint()
                    }
                    onComplete(null)
                } else {
                    onComplete(IllegalStateException("Complaint with id $complaintId not found in local cache"))
                }
            } catch (e: Exception) {
                onComplete(e)
            }
        }
    }

    fun deleteComplaint(
        complaintId: Int, // Hashed ID from UI
        localAdminList: SnapshotStateList<AdminComplaint>,
        onComplete: (Throwable?) -> Unit
    ) {
        coroutineScope.launch {
            try {
                val matchedDto = rawDtos.find { kotlin.math.abs((it.id ?: "").hashCode()) == complaintId }
                val stringId = matchedDto?.id
                if (stringId != null) {
                    withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.deleteComplaint(stringId)
                    }
                    val index = localAdminList.indexOfFirst { it.id == complaintId }
                    if (index != -1) {
                        localAdminList.removeAt(index)
                    }
                    onComplete(null)
                } else {
                    onComplete(IllegalStateException("Complaint with id $complaintId not found in local cache"))
                }
            } catch (e: Exception) {
                onComplete(e)
            }
        }
    }

    fun syncPayments(
        residentId: String? = null,
        localList: SnapshotStateList<PaymentDto>,
        onStart: () -> Unit,
        onComplete: (Throwable?) -> Unit
    ) {
        coroutineScope.launch {
            onStart()
            try {
                val list = withContext(Dispatchers.IO) {
                    if (residentId != null) {
                        RetrofitClient.apiService.getResidentPayments(residentId)
                    } else {
                        RetrofitClient.apiService.getPayments()
                    }
                }
                localList.clear()
                list.sortedByDescending { it.createdAt ?: it.generatedDate ?: "" }.forEach { localList.add(it) }
                onComplete(null)
            } catch (e: Exception) {
                onComplete(e)
            }
        }
    }

    fun syncPendingPayments(
        localList: SnapshotStateList<PaymentDto>,
        onStart: () -> Unit,
        onComplete: (Throwable?) -> Unit
    ) {
        coroutineScope.launch {
            onStart()
            try {
                val list = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getPendingPayments()
                }
                localList.clear()
                list.sortedByDescending { it.createdAt ?: it.generatedDate ?: "" }.forEach { localList.add(it) }
                onComplete(null)
            } catch (e: Exception) {
                onComplete(e)
            }
        }
    }

    fun generateMonthlyBills(
        month: String,
        year: String,
        onComplete: (Throwable?, Int) -> Unit
    ) {
        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.generateMonthlyBills(
                        mapOf("month" to month, "year" to year)
                    )
                }
                val count = (response["totalBills"] as? Number)?.toInt() ?: 0
                onComplete(null, count)
            } catch (e: Exception) {
                onComplete(e, 0)
            }
        }
    }

    fun generateBill(
        paymentDto: PaymentDto,
        onComplete: (Throwable?) -> Unit
    ) {
        coroutineScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.createPayment(paymentDto)
                }
                onComplete(null)
            } catch (e: Exception) {
                onComplete(e)
            }
        }
    }

    fun payBill(
        billId: String,
        paymentApp: String,
        transactionId: String,
        onComplete: (Throwable?) -> Unit
    ) {
        coroutineScope.launch {
            try {
                val body = mapOf(
                    "paymentApp" to paymentApp,
                    "transactionId" to transactionId
                )
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.payBillNew(billId, body)
                }
                onComplete(null)
            } catch (e: Exception) {
                onComplete(e)
            }
        }
    }

    fun syncResidents(
        localList: SnapshotStateList<AdminUser>,
        onStart: () -> Unit,
        onComplete: (Throwable?) -> Unit
    ) {
        coroutineScope.launch {
            onStart()
            try {
                val list = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getAllResidents()
                }
                rawResidentDtos = list
                
                localList.clear()
                list.map { it.toAdminUser() }.forEach { localList.add(it) }
                onComplete(null)
            } catch (e: Exception) {
                onComplete(e)
            }
        }
    }

    fun approveOrRejectResident(
        userId: Int, // Hashed ID from UI
        status: String, // "Approved" or "Rejected"
        localList: SnapshotStateList<AdminUser>,
        onComplete: (Throwable?) -> Unit
    ) {
        coroutineScope.launch {
            try {
                val matchedDto = rawResidentDtos.find { kotlin.math.abs((it.id ?: "").hashCode()) == userId }
                val stringId = matchedDto?.id
                if (stringId != null) {
                    val updated = withContext(Dispatchers.IO) {
                        if (status.equals("Approved", ignoreCase = true)) {
                            RetrofitClient.apiService.approveResident(stringId)
                        } else {
                            RetrofitClient.apiService.rejectResident(stringId)
                        }
                    }
                    
                    val index = localList.indexOfFirst { it.id == userId }
                    if (index != -1) {
                        localList[index] = updated.toAdminUser()
                    }
                    onComplete(null)
                } else {
                    onComplete(IllegalStateException("Resident with id $userId not found in local cache"))
                }
            } catch (e: Exception) {
                onComplete(e)
            }
        }
    }

    fun syncAnalytics(
        onStart: () -> Unit,
        onComplete: (AnalyticsResponse?, Throwable?) -> Unit
    ) {
        coroutineScope.launch {
            onStart()
            try {
                val analytics = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getAnalytics()
                }
                onComplete(analytics, null)
            } catch (e: Exception) {
                onComplete(null, e)
            }
        }
    }
}
