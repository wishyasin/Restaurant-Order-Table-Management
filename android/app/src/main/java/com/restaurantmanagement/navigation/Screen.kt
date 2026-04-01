package com.restaurantmanagement.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Tables : Screen("tables")
    object TableDetail : Screen("table_detail/{tableId}") {
        fun createRoute(tableId: Int) = "table_detail/$tableId"
    }
    object Menu : Screen("menu/{tableId}") {
        fun createRoute(tableId: Int) = "menu/$tableId"
    }

    object DailyReport : Screen("daily_report")
    object AdminPanel : Screen("admin_panel")
}