package com.restaurantmanagement.data.model

import com.google.gson.annotations.SerializedName

data class OrderItem(
    val id: Int,
    val menuItem: MenuItem,
    val quantity: Int,
    val tableId: Int
)

data class Order(
    val id: Int,
    @SerializedName("table_id") val tableId: Int,
    val items: List<OrderItem>,
    val status: OrderStatus,
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalAmount: Double
        get() = items.sumOf { it.menuItem.price * it.quantity }
}

enum class OrderStatus {
    OPEN,
    PAID_CASH,
    PAID_CARD
}