package com.simats.appartmentliving.data

data class AnalyticsResponse(
    val complaints: ComplaintAnalyticsDto,
    val residents: ResidentAnalyticsDto,
    val payments: PaymentAnalyticsDto
)

data class ComplaintAnalyticsDto(
    val total: Int,
    val pending: Int,
    val resolved: Int,
    val averageResolutionTime: Double,
    val byCategory: List<CategoryCountDto>,
    val dailyStats: List<DailyStatDto>,
    val resolutionHistogram: List<HistogramBinDto>
)

data class CategoryCountDto(
    val category: String,
    val count: Int
)

data class DailyStatDto(
    val day: String,
    val date: String,
    val plumbing: Int,
    val electrical: Int,
    val lift: Int,
    val other: Int
)

data class HistogramBinDto(
    val range: String,
    val count: Int
)

data class ResidentAnalyticsDto(
    val total: Int,
    val approved: Int,
    val pending: Int,
    val monthlyRegistrations: List<MonthlyRegistrationDto>
)

data class MonthlyRegistrationDto(
    val month: String,
    val count: Int
)

data class PaymentAnalyticsDto(
    val totalRevenue: Double,
    val paidBills: Int,
    val unpaidBills: Int,
    val collectionRate: Double,
    val monthlyCollections: List<MonthlyCollectionDto>
)

data class MonthlyCollectionDto(
    val month: String,
    val amount: Double
)
