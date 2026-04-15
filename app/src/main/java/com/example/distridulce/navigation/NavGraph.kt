package com.example.distridulce.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.distridulce.model.Client
import com.example.distridulce.ui.catalog.CatalogScreen
import com.example.distridulce.ui.clients.ClientsScreen
import com.example.distridulce.ui.dashboard.DashboardScreen
import com.example.distridulce.ui.history.HistoryScreen
import com.example.distridulce.ui.orders.NewOrderScreen
import com.example.distridulce.ui.orders.OrderBuilderScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }

        // Standalone catalogue — sidebar navigation, no client context.
        composable(
            route = "catalog?clientId={clientId}",
            arguments = listOf(
                navArgument("clientId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            CatalogScreen(clientId = backStackEntry.arguments?.getString("clientId"))
        }

        composable(Screen.Clients.route) {
            ClientsScreen()
        }

        // Step 1 of new order: choose client → navigate to OrderBuilder.
        composable(Screen.NewOrder.route) {
            NewOrderScreen(
                onClientSelected = { client: Client ->
                    navController.navigate(Screen.OrderBuilder.withClient(client.id))
                }
            )
        }

        // Step 2 of new order: browse products + manage cart.
        composable(
            route = "order_builder?clientId={clientId}",
            arguments = listOf(
                navArgument("clientId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            OrderBuilderScreen(clientId = backStackEntry.arguments?.getString("clientId"))
        }

        composable(Screen.History.route) {
            HistoryScreen()
        }
    }
}
