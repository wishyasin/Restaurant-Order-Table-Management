package com.restaurantmanagement.data.repository

import com.restaurantmanagement.data.model.Table
import com.restaurantmanagement.data.remote.PaymentRequest
import com.restaurantmanagement.data.remote.RetrofitClient

class RestaurantRepository {
    private val api = RetrofitClient.apiService

    suspend fun getMenuItems() = api.getMenuItems()
    suspend fun getTables() = api.getTables()
    suspend fun getActiveOrder(tableId: Int) = api.getActiveOrder(tableId)
    suspend fun completePayment(orderId: Int, tableId: Int, status: String) =
        api.completePayment(PaymentRequest(orderId,
            tableId,
            status))
}