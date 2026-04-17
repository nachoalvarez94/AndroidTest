package com.example.distridulce.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Dashboard : Screen(
        route = "dashboard",
        title = "Inicio",
        icon = Icons.Filled.Dashboard
    )

    object Catalog : Screen(
        route = "catalog",
        title = "Catálogo",
        icon = Icons.Filled.Inventory2
    ) {
        /** Full route used when navigating from NewOrder with a pre-selected client. */
        fun withClient(clientId: String) = "catalog?clientId=$clientId"
    }

    object Clients : Screen(
        route = "clients",
        title = "Clientes",
        icon = Icons.Filled.People
    )

    object NewOrder : Screen(
        route = "new_order",
        title = "Nuevo Pedido",
        icon = Icons.Filled.ShoppingCartCheckout
    )

    object Orders : Screen(
        route = "orders",
        title = "Pedidos",
        icon  = Icons.Filled.Assignment
    )

    object History : Screen(
        route = "history",
        title = "Historial",
        icon = Icons.Filled.History
    )

    /**
     * Order-building screen reached from NewOrderScreen after selecting a client.
     * Not shown in the sidebar — accessed only through the NewOrder flow.
     */
    object OrderBuilder : Screen(
        route = "order_builder",
        title = "Nuevo Pedido",
        icon  = Icons.Filled.ShoppingCartCheckout
    ) {
        fun withClient(clientId: String) = "order_builder?clientId=$clientId"
    }

    /**
     * Payment / checkout screen — step 3 of the new-order flow.
     * Not shown in the sidebar.
     */
    object Checkout : Screen(
        route = "checkout",
        title = "Pago",
        icon  = Icons.Filled.Payment
    )

    /**
     * Invoice/factura screen — final step of the new-order flow.
     * Not shown in the sidebar.
     */
    object Invoice : Screen(
        route = "invoice",
        title = "Factura",
        icon  = Icons.Filled.Description
    )

    /**
     * Edit-order screen — reached from OrdersScreen via the "Modificar" button.
     * Only available for non-invoiced orders. Not shown in the sidebar.
     */
    object EditOrder : Screen(
        route = "edit_order",
        title = "Editar Pedido",
        icon  = Icons.Filled.Edit
    ) {
        /** Full route used when navigating to the edit screen with a specific pedido. */
        fun withId(pedidoId: Long) = "edit_order/$pedidoId"
    }

    companion object {
        /** Screens shown in the sidebar navigation. Flow-only screens are intentionally excluded. */
        val all = listOf(Dashboard, Catalog, Clients, NewOrder, Orders, History)
    }
}
