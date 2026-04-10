package com.restaurantmanagement.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurantmanagement.data.model.Order
import com.restaurantmanagement.data.model.OrderItem
import com.restaurantmanagement.data.repository.RestaurantRepository
import kotlinx.coroutines.launch

class OrderViewModel(private val repository: RestaurantRepository = RestaurantRepository()) : ViewModel() {

    var currentOrder = mutableStateOf<Order?>(null)
    var orderItems = mutableStateListOf<OrderItem>()
    var isLoading = mutableStateOf(false)

    fun fetchActiveOrder(tableId: Int) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val order = repository.getActiveOrder(tableId)
                currentOrder.value = order
                orderItems.clear()
                order?.items?.let { orderItems.addAll(it) }
            } catch (e: Exception) {
                currentOrder.value = null
            } finally {
                isLoading.value = false
            }
        }
    }


    fun processPayment(orderId: Int, tableId: Int, status: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {

                val response = repository.completePayment(orderId, tableId, status)

                if (response.isSuccessful) {

                    onComplete()
                } else {
                    println("API Error: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Connection Error: ${e.message}")
            }
        }
    }

}