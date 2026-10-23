package com.restaurantmanagement.data.remote

// Holds the JWT returned by /api/login for the lifetime of the app process.
object SessionManager {
    @Volatile
    var token: String? = null
}
