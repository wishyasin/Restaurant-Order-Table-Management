package com.restaurantmanagement.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurantmanagement.data.model.Table
import com.restaurantmanagement.data.repository.RestaurantRepository
import kotlinx.coroutines.launch


class TableViewModel(private val repository: RestaurantRepository = RestaurantRepository()) : ViewModel() {

    var tables = mutableStateListOf<Table>()
        private set


    var isLoading = mutableStateOf(false)
        private set


    var errorMessage = mutableStateOf<String?>(null)
        private set

    init {
        fetchTables()
    }

    fun fetchTables() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val remoteTables = repository.getTables()
                tables.clear()
                tables.addAll(remoteTables)
            } catch (e: Exception) {
                errorMessage.value = "Connection Error: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }
}