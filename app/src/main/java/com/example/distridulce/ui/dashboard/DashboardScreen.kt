package com.example.distridulce.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            DashboardHeader()
            KpiRow()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RecentOrdersCard(modifier = Modifier.weight(1.5f))
                QuickActionsCard(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DashboardHeader() {
    Column {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Resumen de operaciones del día",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun KpiRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KpiCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.ShoppingCart,
            label = "Pedidos hoy",
            value = "24",
            trendLabel = "+3 vs ayer"
        )
        KpiCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.People,
            label = "Clientes activos",
            value = "142",
            trendLabel = "Este mes"
        )
        KpiCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Inventory2,
            label = "Productos",
            value = "318",
            trendLabel = "En catálogo"
        )
        KpiCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.CheckCircle,
            label = "Pedidos entregados",
            value = "18",
            trendLabel = "De 24 de hoy"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KpiCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    trendLabel: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = trendLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentOrdersCard(modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pedidos recientes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            val orders = listOf(
                Triple("PED-0024", "Supermercado López", "Pendiente"),
                Triple("PED-0023", "Almacén Central", "En camino"),
                Triple("PED-0022", "Distribuidora Norte", "Entregado"),
                Triple("PED-0021", "Minimarket Plaza", "Entregado"),
                Triple("PED-0020", "Bodega Sur", "Entregado"),
            )

            orders.forEachIndexed { index, (id, client, status) ->
                OrderRow(id = id, client = client, status = status)
                if (index < orders.lastIndex) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun OrderRow(id: String, client: String, status: String) {
    val statusColor = when (status) {
        "Entregado" -> MaterialTheme.colorScheme.tertiary
        "En camino" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = id,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = client,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = status,
            style = MaterialTheme.typography.labelMedium,
            color = statusColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionsCard(modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Acciones rápidas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            QuickActionItem("Nuevo pedido")
            Divider()
            QuickActionItem("Agregar cliente")
            Divider()
            QuickActionItem("Ver catálogo")
            Divider()
            QuickActionItem("Exportar informe")
        }
    }
}

@Composable
private fun QuickActionItem(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
