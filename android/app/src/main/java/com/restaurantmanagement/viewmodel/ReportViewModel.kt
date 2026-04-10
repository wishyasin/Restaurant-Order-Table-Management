package com.restaurantmanagement.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurantmanagement.data.remote.DailyReportResponse
import com.restaurantmanagement.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class ReportViewModel : ViewModel() {

    var reportData = mutableStateOf<DailyReportResponse?>(null)
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    fun fetchReport() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val response = RetrofitClient.apiService.getDailyReport()
                if (response.isSuccessful) {
                    reportData.value = response.body()
                } else {
                    errorMessage.value = "Failed to retrieve data: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Connection error: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}