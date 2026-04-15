package com.example.distridulce.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.distridulce.ui.theme.ActionBlue
import com.example.distridulce.ui.theme.ActionGreen
import com.example.distridulce.ui.theme.ActionPurple
import com.example.distridulce.ui.theme.IconAmber
import com.example.distridulce.ui.theme.IconBgAmber
import com.example.distridulce.ui.theme.IconBgBlue
import com.example.distridulce.ui.theme.IconBgGreen
import com.example.distridulce.ui.theme.IconBgPurple
import com.example.distridulce.ui.theme.IconBlue
import com.example.distridulce.ui.theme.IconGreen
import com.example.distridulce.ui.theme.IconPurple
import com.example.distridulce.ui.theme.TextSecondary

// ── Screen ────────────────────────────────────────────────────────────────────

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
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            DashboardHeader()
            MetricRow()
            QuickActionsRow()
            RecentOrdersSection()
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Resumen de operaciones del día",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

// ── Metric cards ──────────────────────────────────────────────────────────────

private data class MetricData(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color
)

@Composable
private fun MetricRow() {
    val metrics = listOf(
        MetricData("Pedidos Hoy",       "8",       Icons.Filled.ShoppingCart,        IconBlue,   IconBgBlue),
        MetricData("Clientes Visitados","12",       Icons.Filled.People,              IconGreen,  IconBgGreen),
        MetricData("Productos Activos", "156",      Icons.Filled.Inventory2,          IconAmber,  IconBgAmber),
        MetricData("Ventas del Mes",    "€24.580",  Icons.Filled.TrendingUp,          IconPurple, IconBgPurple),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        metrics.forEach { metric ->
            MetricCard(
                modifier = Modifier.weight(1f),
                label           = metric.label,
                value           = metric.value,
                icon            = metric.icon,
                iconTint        = metric.iconTint,
                iconBackground  = metric.iconBackground
            )
        }
    }
}

/**
 * Reusable metric KPI card.
 * Layout: value + label on the left, icon container on the right.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ── Quick actions ─────────────────────────────────────────────────────────────

private data class QuickActionData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val backgroundColor: Color
)

@Composable
private fun QuickActionsRow() {
    val actions = listOf(
        QuickActionData(
            title           = "Nuevo Pedido",
            subtitle        = "Crear pedido para un cliente",
            icon            = Icons.Filled.ShoppingCartCheckout,
            backgroundColor = ActionBlue
        ),
        QuickActionData(
            title           = "Ver Catálogo",
            subtitle        = "Explorar productos disponibles",
            icon            = Icons.Filled.Inventory2,
            backgroundColor = ActionPurple
        ),
        QuickActionData(
            title           = "Gestionar Clientes",
            subtitle        = "Ver y editar información de clientes",
            icon            = Icons.Filled.People,
            backgroundColor = ActionGreen
        ),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        actions.forEach { action ->
            QuickActionCard(
                modifier        = Modifier.weight(1f),
                title           = action.title,
                subtitle        = action.subtitle,
                icon            = action.icon,
                backgroundColor = action.backgroundColor
            )
        }
    }
}

/**
 * Reusable coloured action card: icon top-left, title, subtitle.
 */
@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon container with semi-transparent white background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.80f)
                )
            }
        }
    }
}

// ── Recent orders ─────────────────────────────────────────────────────────────

private data class RecentOrder(
    val clientName: String,
    val timeLabel: String,
    val amount: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentOrdersSection() {
    val orders = listOf(
        RecentOrder("Supermercado El Ahorro", "Hace 15 min",  "€485,50"),
        RecentOrder("Tienda La Esquina",      "Hace 1 hora",  "€234,20"),
        RecentOrder("Kiosko Central",         "Hace 2 horas", "€156,80"),
        RecentOrder("Minimercado Rápido",     "Hace 3 horas", "€678,90"),
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Últimos Pedidos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            orders.forEachIndexed { index, order ->
                RecentOrderItem(
                    clientName = order.clientName,
                    timeLabel  = order.timeLabel,
                    amount     = order.amount
                )
                if (index < orders.lastIndex) {
                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * Reusable recent-order row: name + time on the left, amount on the right.
 */
@Composable
fun RecentOrderItem(
    clientName: String,
    timeLabel: String,
    amount: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = clientName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = timeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
