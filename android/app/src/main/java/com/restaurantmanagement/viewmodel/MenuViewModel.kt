package com.restaurantmanagement.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurantmanagement.data.model.MenuItem
import com.restaurantmanagement.data.model.OrderItem
import com.restaurantmanagement.data.remote.OrderItemRequest
import com.restaurantmanagement.data.remote.OrderRequest
import com.restaurantmanagement.data.remote.RetrofitClient
import com.restaurantmanagement.data.repository.RestaurantRepository
import kotlinx.coroutines.launch

class MenuViewModel(private val repository: RestaurantRepository = RestaurantRepository()) : ViewModel() {

    var menuItems = mutableStateListOf<MenuItem>()
        private set

    init {
        fetchMenu()
    }

    fun fetchMenu() {
        viewModelScope.launch {
            try {
                val remoteMenu = repository.getMenuItems()
                menuItems.clear()
                menuItems.addAll(remoteMenu)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun placeOrder(tableId: Int, items: List<OrderItem>, existingOrderId: Int?, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val orderRequest = OrderRequest(
                    table_id = tableId,
                    items = items.map { OrderItemRequest(it.menuItem.id, it.quantity) }
                )

                val response = if (existingOrderId != null) {
                    RetrofitClient.apiService.addItemsToOrder(existingOrderId, orderRequest)
                } else {
                    RetrofitClient.apiService.createOrder(orderRequest)
                }

                if (response.isSuccessful) {
                    onComplete()
                } else {
                    println("Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}