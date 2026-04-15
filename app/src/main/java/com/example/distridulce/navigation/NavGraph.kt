package com.example.distridulce.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.distridulce.ui.catalog.CatalogScreen
import com.example.distridulce.ui.clients.ClientsScreen
import com.example.distridulce.ui.dashboard.DashboardScreen
import com.example.distridulce.ui.history.HistoryScreen
import com.example.distridulce.ui.orders.NewOrderScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }
        composable(Screen.Catalog.route) {
            CatalogScreen()
        }
        composable(Screen.Clients.route) {
            ClientsScreen()
        }
        composable(Screen.NewOrder.route) {
            NewOrderScreen()
        }
        composable(Screen.History.route) {
            HistoryScreen()
        }
    }
}
