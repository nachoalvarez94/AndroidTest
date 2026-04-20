package com.example.distridulce.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.distridulce.ui.theme.ActionBlue
import com.example.distridulce.ui.theme.ActionGreen
import com.example.distridulce.ui.theme.ActionPurple
import com.example.distridulce.ui.theme.BrandBlue
import com.example.distridulce.ui.theme.IconAmber
import com.example.distridulce.ui.theme.IconBgAmber
import com.example.distridulce.ui.theme.IconBgBlue
import com.example.distridulce.ui.theme.IconBgGreen
import com.example.distridulce.ui.theme.IconBgPurple
import com.example.distridulce.ui.theme.IconBlue
import com.example.distridulce.ui.theme.IconGreen
import com.example.distridulce.ui.theme.IconPurple
import com.example.distridulce.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * Dashboard / Inicio screen.
 *
 * Loads four backend endpoints in parallel via [DashboardViewModel] and renders:
 * - KPI metric cards (real data)
 * - Quick-action cards (navigable; no duplication of the order-creation flow)
 * - Recent orders list (real backend pedidos)
 *
 * Navigation lambdas let [NavGraph] control routing without coupling this
 * composable to [NavController].
 *
 * Future readiness: when per-user login is added, pass the current user to
 * [DashboardViewModel] and add server-side filtering — no UI changes needed.
 */
@Composable
fun DashboardScreen(
    onNavigateToOrders:  () -> Unit = {},
    onNavigateToCatalog: () -> Unit = {},
    onNavigateToClients: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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

            // Metric cards — show real values from the ViewModel; "—" while loading.
            val metrics = (uiState as? DashboardUiState.Success)?.metrics
            MetricRow(metrics = metrics)

            // Quick actions — always visible; navigation callbacks from NavGraph.
            QuickActionsRow(
                onPendientesClick = onNavigateToOrders,
                onCatalogoClick   = onNavigateToCatalog,
                onClientesClick   = onNavigateToClients
            )

            // Recent orders section varies by state.
            when (val state = uiState) {
                is DashboardUiState.Loading ->
                    RecentOrdersLoading()

                is DashboardUiState.Success ->
                    RecentOrdersSection(pedidos = state.recentPedidos)

                is DashboardUiState.Error ->
                    DashboardErrorCard(
                        message = state.message,
                        onRetry = viewModel::refresh
                    )
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text       = "Dashboard",
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text  = "Resumen de operaciones del día",
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

/**
 * Renders the four KPI metric cards.
 *
 * @param metrics Real data from the ViewModel; null while loading (cards show "—").
 */
@Composable
private fun MetricRow(metrics: DashboardMetrics?) {
    val placeholder = "—"

    val cards = listOf(
        MetricData(
            label           = "Pedidos Hoy",
            value           = metrics?.pedidosHoy?.toString() ?: placeholder,
            icon            = Icons.Filled.ShoppingCart,
            iconTint        = IconBlue,
            iconBackground  = IconBgBlue
        ),
        MetricData(
            label           = "Pdte. Facturar",
            value           = metrics?.pendientesFacturar?.toString() ?: placeholder,
            icon            = Icons.Filled.Assignment,
            iconTint        = IconGreen,
            iconBackground  = IconBgGreen
        ),
        MetricData(
            label           = "Productos Activos",
            value           = metrics?.productosActivos?.toString() ?: placeholder,
            icon            = Icons.Filled.Inventory2,
            iconTint        = IconAmber,
            iconBackground  = IconBgAmber
        ),
        MetricData(
            label           = "Ventas del Mes",
            value           = metrics?.let { formatVentas(it.ventasMes) } ?: placeholder,
            icon            = Icons.Filled.TrendingUp,
            iconTint        = IconPurple,
            iconBackground  = IconBgPurple
        ),
    )

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        cards.forEach { card ->
            MetricCard(
                modifier       = Modifier.weight(1f),
                label          = card.label,
                value          = card.value,
                icon           = card.icon,
                iconTint       = card.iconTint,
                iconBackground = card.iconBackground
            )
        }
    }
}

/** Formats a monetary amount for the KPI card — e.g. "€ 24.580" (European style). */
private fun formatVentas(amount: Double): String {
    val formatted = String.format(Locale("es", "ES"), "%,.0f", amount)
    return "€ $formatted"
}

/**
 * Reusable KPI card.
 * Layout: value + label on the left, coloured icon on the right.
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
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text       = value,
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text  = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Box(
                modifier         = Modifier
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

/**
 * Three navigable action cards.
 *
 * "Nuevo Pedido" has been removed — the order creation flow starts from the
 * Clients screen, not from the Dashboard.  "Pedidos Pendientes" replaces it
 * and surfaces the most actionable information: orders waiting to be invoiced.
 */
@Composable
private fun QuickActionsRow(
    onPendientesClick: () -> Unit,
    onCatalogoClick:   () -> Unit,
    onClientesClick:   () -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            modifier        = Modifier.weight(1f),
            title           = "Pdtes. Facturar",
            subtitle        = "Ver pedidos sin factura",
            icon            = Icons.Filled.Assignment,
            backgroundColor = ActionBlue,
            onClick         = onPendientesClick
        )
        QuickActionCard(
            modifier        = Modifier.weight(1f),
            title           = "Ver Catálogo",
            subtitle        = "Explorar productos disponibles",
            icon            = Icons.Filled.Inventory2,
            backgroundColor = ActionPurple,
            onClick         = onCatalogoClick
        )
        QuickActionCard(
            modifier        = Modifier.weight(1f),
            title           = "Gestionar Clientes",
            subtitle        = "Ver y editar información de clientes",
            icon            = Icons.Filled.People,
            backgroundColor = ActionGreen,
            onClick         = onClientesClick
        )
    }
}

/**
 * Reusable coloured action card: icon top-left, title, subtitle.
 * The [onClick] callback is fired when the whole card is tapped.
 */
@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier            = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon container with semi-transparent white background
            Box(
                modifier         = Modifier
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
                    text       = title,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.80f)
                )
            }
        }
    }
}

// ── Recent orders ─────────────────────────────────────────────────────────────

/** Loading placeholder shown while pedidos are being fetched. */
@Composable
private fun RecentOrdersLoading() {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text       = "Últimos Pedidos",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(32.dp),
                    color       = BrandBlue,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

/** Error state card with a message and a retry button. */
@Composable
private fun DashboardErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint               = Color(0xFFDC2626),
                modifier           = Modifier.size(36.dp)
            )
            Text(
                text       = "No se pudo cargar el dashboard",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text  = message,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            OutlinedButton(onClick = onRetry) {
                Text("Reintentar", color = BrandBlue)
            }
        }
    }
}

/**
 * Recent orders section — shows the last 5 pedidos from the backend.
 *
 * When [pedidos] is empty the card displays a "sin pedidos" message
 * rather than an empty list.
 */
@Composable
private fun RecentOrdersSection(pedidos: List<PedidoSummary>) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text       = "Últimos Pedidos",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )

            if (pedidos.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text  = "No hay pedidos registrados todavía.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                pedidos.forEachIndexed { index, pedido ->
                    RecentOrderItem(
                        clientName = pedido.clienteName,
                        timeLabel  = "Pedido #${pedido.numero}  ·  ${formatPedidoDate(pedido.fecha)}",
                        amount     = "€ %.2f".format(pedido.totalFinal)
                    )
                    if (index < pedidos.lastIndex) {
                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Formats a backend fecha string ("yyyy-MM-dd" or "yyyy-MM-ddTHH:mm:ss")
 * into a compact label, e.g. "20 abr" or "20 abr 10:30".
 */
private fun formatPedidoDate(fecha: String): String {
    val locale = Locale("es", "ES")
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss" to "dd MMM HH:mm",
        "yyyy-MM-dd'T'HH:mm:ss.SSS" to "dd MMM HH:mm",
        "yyyy-MM-dd" to "dd MMM"
    )
    for ((input, output) in patterns) {
        try {
            val parsed = SimpleDateFormat(input, locale).parse(fecha)
            if (parsed != null) return SimpleDateFormat(output, locale).format(parsed)
        } catch (_: Exception) { /* try next */ }
    }
    return fecha   // fallback: raw string
}

/**
 * Reusable recent-order row: client name + order label on the left, amount on the right.
 */
@Composable
fun RecentOrderItem(
    clientName: String,
    timeLabel:  String,
    amount:     String
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text       = clientName,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text  = timeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text       = amount,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface
        )
    }
}
