package com.restaurantmanagement.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.restaurantmanagement.ui.screens.*
import com.restaurantmanagement.viewmodel.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }

        composable(
            route = "${Screen.Tables.route}/{userRole}",
            arguments = listOf(navArgument("userRole") { type = NavType.StringType })
        ) { backStackEntry ->
            val userRole = backStackEntry.arguments?.getString("userRole") ?: "STAFF"
            val tableViewModel: TableViewModel = viewModel()
            TablesScreen(navController, tableViewModel, userRole)
        }

        composable(
            route = Screen.TableDetail.route,
            arguments = listOf(navArgument("tableId") { type = NavType.IntType })
        ) { backStackEntry ->
            val tableId = backStackEntry.arguments?.getInt("tableId") ?: 0
            val orderViewModel: OrderViewModel = viewModel()
            TableDetailScreen(navController, tableId, orderViewModel)
        }

        composable(
            route = Screen.Menu.route,
            arguments = listOf(navArgument("tableId") { type = NavType.IntType })
        ) { backStackEntry ->
            val tableId = backStackEntry.arguments?.getInt("tableId") ?: 0
            val menuViewModel: MenuViewModel = viewModel()
            val orderViewModel: OrderViewModel = viewModel()
            MenuScreen(navController, tableId, menuViewModel, orderViewModel)
        }

        composable(Screen.DailyReport.route) {
            val reportViewModel: ReportViewModel = viewModel()
            DailyReportScreen(navController, reportViewModel)
        }

        composable(Screen.AdminPanel.route) {
            val menuViewModel: MenuViewModel = viewModel()
            val tableViewModel: TableViewModel = viewModel()
            val adminViewModel: AdminViewModel = viewModel()
            AdminPanelScreen(
                navController = navController,
                menuViewModel = menuViewModel,
                tableViewModel = tableViewModel,
                adminViewModel = adminViewModel
            )
        }
    }
}