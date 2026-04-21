package com.restaurantmanagement.data.model

data class User(
    val id: Int,
    val username: String,
    val password: String,
    val role: UserRole,
    val email: String?
)

enum class UserRole {
    ADMIN,
    STAFF
}
