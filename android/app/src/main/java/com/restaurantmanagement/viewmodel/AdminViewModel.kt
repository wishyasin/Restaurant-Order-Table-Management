package com.restaurantmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurantmanagement.data.remote.MenuItemRequest
import com.restaurantmanagement.data.remote.RetrofitClient
import com.restaurantmanagement.data.remote.TableRequest
import com.restaurantmanagement.data.repository.RestaurantRepository
import kotlinx.coroutines.launch

class AdminViewModel(private val repository: RestaurantRepository = RestaurantRepository()) : ViewModel() {

    fun saveMenuItem(id: Int?, name: String, price: Double, category: String, description: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val request = MenuItemRequest(name, price, category, description)
                val response = if (id == null) {
                    RetrofitClient.apiService.addMenuItem(request)
                } else {
                    RetrofitClient.apiService.updateMenuItem(id, request)
                }

                if (response.isSuccessful) {
                    onComplete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteMenuItem(id: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteMenuItem(id)
                if (response.isSuccessful) onComplete()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun saveTable(number: Int, capacity: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.addTable(TableRequest(number,
                    capacity))
                if (response.isSuccessful) onComplete()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun deleteTable(id: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {

                val response = RetrofitClient.apiService.deleteTable(id)
                if (response.isSuccessful) {
                    onComplete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}