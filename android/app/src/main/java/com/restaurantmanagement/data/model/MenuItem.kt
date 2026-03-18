package com.restaurantmanagement.data.model

data class MenuItem(
    val id: Int,
    val name: String,
    val price: Double,
    val category: String,
    val isAvailable: Boolean = true
)