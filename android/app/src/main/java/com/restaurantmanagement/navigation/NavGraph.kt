package com.restaurantmanagement.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.restaurantmanagement.ui.screens.AdminPanelScreen
import com.restaurantmanagement.ui.screens.DailyReportScreen
import com.restaurantmanagement.ui.screens.LoginScreen
import com.restaurantmanagement.ui.screens.MenuScreen
import com.restaurantmanagement.ui.screens.TableDetailScreen
import com.restaurantmanagement.ui.screens.TablesScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Tables.route) {
            TablesScreen(navController)
        }
        composable(
            route = Screen.TableDetail.route,
            arguments = listOf(navArgument("tableId") { type = NavType.IntType })
        ) { backStackEntry ->
            val tableId = backStackEntry.arguments?.getInt("tableId") ?: 0
            TableDetailScreen(navController, tableId)
        }
        composable(
            route = Screen.Menu.route,
            arguments = listOf(navArgument("tableId") { type = NavType.IntType })
        ) { backStackEntry ->
            val tableId = backStackEntry.arguments?.getInt("tableId") ?: 0
            MenuScreen(navController, tableId)
        }

        composable(Screen.DailyReport.route) {
            DailyReportScreen(navController)
        }
        composable(Screen.AdminPanel.route) {
            AdminPanelScreen(navController)
        }
    }
}