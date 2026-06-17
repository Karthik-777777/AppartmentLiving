package com.simats.appartmentliving.data

import retrofit2.http.*

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): UserDto

    @POST("api/auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): SendOtpResponse

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): VerifyOtpResponse

    @GET("https://maps.googleapis.com/maps/api/place/textsearch/json")
    suspend fun getNearbyPlaces(
        @Query("query") query: String,
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("key") key: String
    ): GooglePlacesResponse
    @GET("api/complaints")
    suspend fun getComplaints(): List<ComplaintDto>

    @POST("api/complaints")
    suspend fun createComplaint(@Body complaint: ComplaintDto): ComplaintDto

    @PUT("api/complaints/{id}")
    suspend fun updateComplaint(
        @Path("id") id: String,
        @Body updates: Map<String, String>
    ): ComplaintDto

    @POST("api/complaints/create")
    suspend fun createComplaintNew(@Body complaint: ComplaintDto): ComplaintDto

    @GET("api/complaints/resident/{residentId}")
    suspend fun getResidentComplaints(@Path("residentId") residentId: String): List<ComplaintDto>

    @GET("api/complaints/all")
    suspend fun getAllComplaints(): List<ComplaintDto>

    @PUT("api/complaints/{id}/status")
    suspend fun updateComplaintStatus(
        @Path("id") id: String,
        @Body updates: Map<String, String>
    ): ComplaintDto

    @DELETE("api/complaints/{id}")
    suspend fun deleteComplaint(@Path("id") id: String): Void?

    @GET("api/payments")
    suspend fun getPayments(): List<PaymentDto>

    @GET("api/payments/pending")
    suspend fun getPendingPayments(): List<PaymentDto>

    @GET("api/payments/resident/{residentId}")
    suspend fun getResidentPayments(@Path("residentId") residentId: String): List<PaymentDto>

    @POST("api/payments/generate")
    suspend fun generateMonthlyBills(@Body body: Map<String, String>): Map<String, Any>

    @PUT("api/payments/{id}/pay")
    suspend fun payBillNew(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): PaymentDto

    @POST("api/payments")
    suspend fun createPayment(@Body payment: PaymentDto): PaymentDto

    @PUT("api/payments/{id}")
    suspend fun updatePayment(
        @Path("id") id: String,
        @Body updates: Map<String, Any>
    ): PaymentDto

    @GET("api/residents")
    suspend fun getResidents(): List<ResidentDto>

    @PUT("api/residents/{id}")
    suspend fun updateResidentStatus(
        @Path("id") id: String,
        @Body updates: Map<String, String>
    ): ResidentDto

    @GET("api/residents/pending")
    suspend fun getPendingResidents(): List<ResidentDto>

    @GET("api/residents/all")
    suspend fun getAllResidents(): List<ResidentDto>

    @PUT("api/residents/{id}/approve")
    suspend fun approveResident(@Path("id") id: String): ResidentDto

    @PUT("api/residents/{id}/reject")
    suspend fun rejectResident(@Path("id") id: String): ResidentDto

    @GET("api/analytics")
    suspend fun getAnalytics(): AnalyticsResponse

    @POST("api/notices")
    suspend fun createNotice(@Body notice: NoticeDto): NoticeDto

    @GET("api/notices")
    suspend fun getNotices(): List<NoticeDto>

    @GET("api/notices/latest")
    suspend fun getLatestNotices(): List<NoticeDto>

    @DELETE("api/notices/{id}")
    suspend fun deleteNotice(@Path("id") id: String): Map<String, Any>
}
