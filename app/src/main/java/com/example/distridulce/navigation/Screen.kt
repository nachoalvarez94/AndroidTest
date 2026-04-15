package com.example.distridulce.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
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
    )

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

    object History : Screen(
        route = "history",
        title = "Historial",
        icon = Icons.Filled.History
    )

    companion object {
        val all = listOf(Dashboard, Catalog, Clients, NewOrder, History)
    }
}
