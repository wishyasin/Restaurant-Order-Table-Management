package com.restaurantmanagement.data.model

data class MenuItem(
    val id: Int,
    val name: String,
    val price: Double,
    val category: MenuCategory,
    val description: String = "",
    val isAvailable: Boolean = true
)

enum class MenuCategory {
    FOOD,
    DRINK,
    DESSERT,
    SNACK
}