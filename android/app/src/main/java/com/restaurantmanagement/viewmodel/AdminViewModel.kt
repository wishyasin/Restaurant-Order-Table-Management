package com.restaurantmanagement.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurantmanagement.data.model.Table
import com.restaurantmanagement.data.model.User
import com.restaurantmanagement.data.remote.MenuItemRequest
import com.restaurantmanagement.data.remote.RetrofitClient
import com.restaurantmanagement.data.remote.TableRequest
import com.restaurantmanagement.data.remote.UserAddRequest
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

    fun saveTable(table: Table?, number: Int, capacity: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val request = TableRequest(number, capacity)
                val response = if (table == null) {

                    RetrofitClient.apiService.addTable(request)
                } else {

                    RetrofitClient.apiService.updateTable(table.id, request)
                }

                if (response.isSuccessful) {
                    fetchTables()
                    onComplete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

    var tables = mutableStateListOf<Table>()
        private set
    var users = mutableStateListOf<User>()
        private set
    init {
        fetchUsers()
        fetchTables()
    }

    fun fetchTables() {
        viewModelScope.launch {
            try {
                val remoteTables = RetrofitClient.apiService.getTables()
                tables.clear()
                tables.addAll(remoteTables)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                val remoteUsers = RetrofitClient.apiService.getUsers()
                users.clear()
                users.addAll(remoteUsers)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun saveUser(username: String, email: String, pass: String, role: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val request = UserAddRequest(username, pass, role, email)
                val response = RetrofitClient.apiService.addUser(request)
                if (response.isSuccessful) {
                    fetchUsers()
                    onComplete()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}