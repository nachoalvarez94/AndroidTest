package com.example.distridulce.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.distridulce.model.Client
import com.example.distridulce.model.OrderSession
import com.example.distridulce.ui.catalog.CatalogScreen
import com.example.distridulce.ui.clients.ClientsScreen
import com.example.distridulce.ui.dashboard.DashboardScreen
import com.example.distridulce.ui.history.HistoryScreen
import com.example.distridulce.ui.orders.CheckoutScreen
import com.example.distridulce.ui.orders.EditOrderScreen
import com.example.distridulce.ui.orders.EditOrderViewModel
import com.example.distridulce.ui.orders.InvoiceScreen
import com.example.distridulce.ui.orders.NewOrderScreen
import com.example.distridulce.ui.orders.OrderBuilderScreen
import com.example.distridulce.ui.orders.OrdersScreen
import com.example.distridulce.ui.orders.OrdersViewModel

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
                    // Store the full Client object so OrderBuilderScreen can display
                    // the client name without a second network call.
                    OrderSession.client = client
                    navController.navigate(Screen.OrderBuilder.withClient(client.id))
                }
            )
        }

        // Step 1 of new order: choose client → navigate to OrderBuilder.
        composable(Screen.NewOrder.route) {
            NewOrderScreen(
                onClientSelected = { client: Client ->
                    // Same as above — seed the session before entering the builder.
                    OrderSession.client = client
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
                onConfirm = { navController.navigate(Screen.Checkout.route) },
                onCancel  = {
                    // Clear in-progress order data, then simply pop back to wherever
                    // the user came from (either NewOrderScreen or ClientsScreen).
                    // Using popBackStack() — not navigate() — so the back stack stays
                    // clean and the Sidebar's saveState/restoreState won't resurrect
                    // OrderBuilder or any orphan NewOrder entry.
                    OrderSession.clear()
                    navController.popBackStack()
                }
            )
        }

        // Step 3 of new order: choose payment method.
        // On confirm: the pedido is created and the user lands on OrdersScreen
        // (NOT InvoiceScreen — invoicing is done explicitly from OrdersScreen).
        composable(Screen.Checkout.route) {
            CheckoutScreen(
                onConfirm = {
                    navController.navigate(Screen.Orders.route) {
                        // Pop ALL the way back to the graph's start destination
                        // (Dashboard), regardless of how the order flow was entered.
                        //
                        // Using popUpTo(NewOrder) was a silent no-op when the flow
                        // started from ClientsScreen — NewOrder is never in the back
                        // stack in that case. That left Clients → OrderBuilder →
                        // Checkout in the stack, which the Sidebar then saved as the
                        // "Clients tab" state and restored on the next sidebar tap,
                        // landing the user back at Orders instead of Clients.
                        //
                        // Popping to the start destination always works and produces
                        // a clean stack: Dashboard → Orders.
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onBack   = { navController.popBackStack() },
                onCancel = {
                    OrderSession.clear()
                    // Pop Checkout + OrderBuilder back to whichever screen started
                    // the flow.  Try NewOrder first (sidebar entry); fall back to
                    // Clients (card-button entry).  Both leave the stack clean so the
                    // Sidebar's saveState/restoreState won't resurrect stale screens.
                    if (!navController.popBackStack(Screen.NewOrder.route, inclusive = false)) {
                        navController.popBackStack(Screen.Clients.route, inclusive = false)
                    }
                }
            )
        }

        // Invoice viewer — only reached from OrdersScreen after a pedido has been
        // invoiced.  InvoiceViewModel uses GET (not POST) because OrdersScreen
        // always seeds OrderSession.facturaId before navigating here.
        composable(Screen.Invoice.route) {
            InvoiceScreen(
                onBack     = { navController.popBackStack() },
                onNewOrder = {
                    // Jump straight to client selection and clear the back stack.
                    navController.navigate(Screen.NewOrder.route) {
                        popUpTo(Screen.NewOrder.route) { inclusive = true }
                    }
                }
            )
        }

        // Orders management — list of all orders with invoicing actions.
        //
        // The ViewModel is created at the back-stack-entry scope so that the
        // edit-order screen can signal back a refresh via savedStateHandle.
        composable(Screen.Orders.route) { backStackEntry ->
            val ordersViewModel: OrdersViewModel = viewModel(backStackEntry)

            // Refresh signal set by EditOrderScreen on a successful save.
            val shouldRefresh by backStackEntry.savedStateHandle
                .getStateFlow("should_refresh_orders", false)
                .collectAsState()
            LaunchedEffect(shouldRefresh) {
                if (shouldRefresh) {
                    ordersViewModel.loadData()
                    backStackEntry.savedStateHandle["should_refresh_orders"] = false
                }
            }

            OrdersScreen(
                onViewFactura = { facturaId ->
                    // Seed the session so InvoiceViewModel uses GET instead of POST.
                    OrderSession.facturaId      = facturaId
                    OrderSession.pedidoId       = null
                    OrderSession.paymentSummary = null
                    navController.navigate(Screen.Invoice.route)
                },
                onModificar = { pedidoId ->
                    navController.navigate(Screen.EditOrder.withId(pedidoId))
                },
                viewModel = ordersViewModel
            )
        }

        // Edit order — reached from OrdersScreen "Modificar" on non-invoiced orders.
        // On save: signals Orders to refresh via savedStateHandle, then pops back.
        composable(
            route = "edit_order/{pedidoId}",
            arguments = listOf(
                navArgument("pedidoId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val pedidoId = backStackEntry.arguments!!.getLong("pedidoId")
            EditOrderScreen(
                viewModel = viewModel(
                    factory = EditOrderViewModel.Factory(pedidoId)
                ),
                onSaved = {
                    // Signal the Orders back-stack entry to reload its list.
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("should_refresh_orders", true)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen()
        }
    }
}
