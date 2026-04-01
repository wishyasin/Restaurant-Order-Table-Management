package com.restaurantmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.restaurantmanagement.navigation.NavGraph
import com.restaurantmanagement.ui.theme.RestaurantManagementTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RestaurantManagementTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}