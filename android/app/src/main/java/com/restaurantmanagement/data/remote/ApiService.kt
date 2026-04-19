package com.restaurantmanagement.data.remote

import com.google.gson.annotations.SerializedName
import com.restaurantmanagement.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<User>

    @POST("orders/{orderId}/items")

    suspend fun addItemsToOrder(@Path("orderId") orderId: Int, @Body request: OrderRequest): Response<Unit>
    @GET("menu")
    suspend fun getMenuItems(): List<MenuItem>

    @GET("tables")
    suspend fun getTables(): List<Table>

    @POST("menu")
    suspend fun addMenuItem(@Body item: MenuItemRequest): Response<Unit>

    @PUT("menu/{id}")
    suspend fun updateMenuItem(@Path("id") id: Int, @Body item: MenuItemRequest): Response<Unit>

    @DELETE("menu/{id}")
    suspend fun deleteMenuItem(@Path("id") id: Int): Response<Unit>

    @POST("tables")
    suspend fun addTable(@Body table: TableRequest): Response<Unit>

    @DELETE("tables/{id}")
    suspend fun deleteTable(@Path("id") id: Int): Response<Unit>

    @POST("orders")
    suspend fun createOrder(@Body orderRequest: OrderRequest): Response<OrderResponse>

    @GET("orders/table/{tableId}")
    suspend fun getActiveOrder(@Path("tableId") tableId: Int): Order?

    @POST("orders/payment")
    suspend fun completePayment(@Body paymentRequest: PaymentRequest): Response<Unit>

    @GET("reports/daily")
    suspend fun getDailyReport(): Response<DailyReportResponse>
}
data class LoginRequest(val username: String, val password: String)

data class OrderRequest(
    val table_id: Int,
    val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    val menu_item_id: Int,
    val quantity: Int
)

data class OrderResponse(
    val message: String,
    val orderId: Int
)
data class MenuItemRequest(
    val name: String,
    val price: Double,
    val category: String,
    val description: String
)

data class TableRequest(val number: Int, val capacity: Int)

data class PaymentRequest(
    val orderId: Int,
    val tableId: Int,
    val status: String
)
data class DailyReportResponse(
    val summary: ReportSummary,
    val categorySales: List<CategorySale>,
    val topItems: List<TopItem>
)

data class ReportSummary(
    @SerializedName("total_revenue") val totalRevenue: Double,
    @SerializedName("total_orders") val totalOrders: Int,
    @SerializedName("open_orders") val openOrders: Int,
    @SerializedName("pending_amount") val pendingAmount: Double
)

data class CategorySale(
    val category: String,
    val qty: Int,
    val total: Double
)

data class TopItem(
    val name: String,
    val category: String,
    val qty: Int,
    val total: Double
)