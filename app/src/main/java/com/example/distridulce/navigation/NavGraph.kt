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
import com.example.distridulce.ui.orders.CheckoutScreen
import com.example.distridulce.ui.orders.InvoiceScreen
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

        // Clients list — "Nuevo Pedido" skips client selection and goes straight to OrderBuilder.
        composable(Screen.Clients.route) {
            ClientsScreen(
                onNewOrder = { client: Client ->
                    navController.navigate(Screen.OrderBuilder.withClient(client.id))
                }
            )
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
            OrderBuilderScreen(
                clientId  = backStackEntry.arguments?.getString("clientId"),
                onConfirm = { navController.navigate(Screen.Checkout.route) }
            )
        }

        // Step 3 of new order: choose payment method.
        composable(Screen.Checkout.route) {
            CheckoutScreen(
                onConfirm = { navController.navigate(Screen.Invoice.route) },
                onBack    = { navController.popBackStack() }
            )
        }

        // Step 4 of new order: invoice / factura.
        composable(Screen.Invoice.route) {
            InvoiceScreen(
                onNewOrder = {
                    // Clear the back stack back to NewOrder so the user starts fresh.
                    navController.navigate(Screen.NewOrder.route) {
                        popUpTo(Screen.NewOrder.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen()
        }
    }
}
