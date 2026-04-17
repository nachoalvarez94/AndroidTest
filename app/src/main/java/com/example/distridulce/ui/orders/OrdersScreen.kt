@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.distridulce.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
// All icons used: Assignment, CheckCircle, Close, CloudOff, Construction, Description,
// Edit, Error, Inbox, Person, Receipt, Refresh, Schedule, Share, Visibility, Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// ── Palette ───────────────────────────────────────────────────────────────────

private val ColorSurface       = Color(0xFFF8F9FA)
private val ColorCard          = Color.White
private val ColorPrimary       = Color(0xFF1565C0)
private val ColorPrimaryLight  = Color(0xFFE3F2FD)
private val ColorSuccess       = Color(0xFF2E7D32)
private val ColorSuccessLight  = Color(0xFFE8F5E9)
private val ColorWarning       = Color(0xFFF57F17)
private val ColorWarningLight  = Color(0xFFFFFDE7)
private val ColorError         = Color(0xFFC62828)
private val ColorErrorLight    = Color(0xFFFFEBEE)
private val ColorNeutral       = Color(0xFF546E7A)
private val ColorNeutralLight  = Color(0xFFECEFF1)
private val ColorDivider       = Color(0xFFE0E0E0)
private val ColorTextPrimary   = Color(0xFF212121)
private val ColorTextSecondary = Color(0xFF757575)

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun OrdersScreen(
    onViewFactura: (facturaId: Long) -> Unit,
    viewModel: OrdersViewModel = viewModel()
) {
    val uiState     by viewModel.uiState.collectAsState()
    val billing     by viewModel.billingAction.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorSurface)
    ) {
        OrdersHeader(
            uiState     = uiState,
            onToggle    = viewModel::toggleFilter,
            onRefresh   = viewModel::loadData
        )

        Divider(color = ColorDivider)

        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is OrdersUiState.Loading -> OrdersLoadingContent()
                is OrdersUiState.Error   -> OrdersErrorContent(state.message, viewModel::loadData)
                is OrdersUiState.Success -> {
                    if (state.pedidos.isEmpty()) {
                        OrdersEmptyContent(state.filterLast24h, viewModel::toggleFilter)
                    } else {
                        OrdersList(
                            pedidos       = state.pedidos,
                            billing       = billing,
                            onFacturar    = viewModel::facturarPedido,
                            onViewFactura = onViewFactura,
                            onDismissError = viewModel::dismissBillingError
                        )
                    }
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun OrdersHeader(
    uiState: OrdersUiState,
    onToggle: () -> Unit,
    onRefresh: () -> Unit
) {
    val filterActive = (uiState as? OrdersUiState.Success)?.filterLast24h ?: true
    val count        = (uiState as? OrdersUiState.Success)?.pedidos?.size ?: 0
    val invoiced     = (uiState as? OrdersUiState.Success)?.pedidos?.count { it.factura != null } ?: 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorCard)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text       = "Pedidos",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = ColorTextPrimary
                )
                if (uiState is OrdersUiState.Success) {
                    Text(
                        text     = "$count pedidos · $invoiced facturados",
                        fontSize = 13.sp,
                        color    = ColorTextSecondary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Filter chips
                FilterChip(
                    selected  = filterActive,
                    onClick   = { if (!filterActive) onToggle() },
                    label     = { Text("Últimas 24 h", fontSize = 13.sp) },
                    colors    = FilterChipDefaults.filterChipColors(
                        selectedContainerColor    = ColorPrimaryLight,
                        selectedLabelColor        = ColorPrimary
                    )
                )
                FilterChip(
                    selected  = !filterActive,
                    onClick   = { if (filterActive) onToggle() },
                    label     = { Text("Todos", fontSize = 13.sp) },
                    colors    = FilterChipDefaults.filterChipColors(
                        selectedContainerColor    = ColorPrimaryLight,
                        selectedLabelColor        = ColorPrimary
                    )
                )

                // Refresh
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector  = Icons.Filled.Refresh,
                        contentDescription = "Actualizar",
                        tint         = ColorNeutral
                    )
                }
            }
        }
    }
}

// ── List ──────────────────────────────────────────────────────────────────────

@Composable
private fun OrdersList(
    pedidos: List<PedidoUiModel>,
    billing: BillingAction,
    onFacturar: (Long) -> Unit,
    onViewFactura: (Long) -> Unit,
    onDismissError: () -> Unit
) {
    // Show billing error snackbar
    val errorPedido = (billing as? BillingAction.Error)
    if (errorPedido != null) {
        BillingErrorBanner(
            message   = errorPedido.message,
            onDismiss = onDismissError
        )
    }

    LazyColumn(
        contentPadding    = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = pedidos, key = { it.pedidoId }) { pedido ->
            PedidoCard(
                pedido        = pedido,
                billing       = billing,
                onFacturar    = onFacturar,
                onViewFactura = onViewFactura
            )
        }
    }
}

// ── Pedido card ───────────────────────────────────────────────────────────────

@Composable
private fun PedidoCard(
    pedido: PedidoUiModel,
    billing: BillingAction,
    onFacturar: (Long) -> Unit,
    onViewFactura: (Long) -> Unit
) {
    var showModifyDialog by remember { mutableStateOf(false) }
    var showShareDialog  by remember { mutableStateOf(false) }

    val isBillingThis = billing is BillingAction.InProgress &&
            (billing as BillingAction.InProgress).pedidoId == pedido.pedidoId
    val hasBillingError = billing is BillingAction.Error &&
            (billing as BillingAction.Error).pedidoId == pedido.pedidoId

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = ColorCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Top row: numero + fecha + total ──────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "PED-%05d".format(pedido.numero),
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = ColorTextPrimary
                    )
                    EstadoBadge(pedido.estado)
                    if (pedido.factura != null) {
                        FacturadoBadge()
                    }
                }

                Text(
                    text       = "€ %.2f".format(pedido.total),
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color      = ColorPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ── Client + date ────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Person,
                        contentDescription = null,
                        modifier           = Modifier.size(14.dp),
                        tint               = ColorTextSecondary
                    )
                    Text(
                        text     = pedido.clienteNombre,
                        fontSize = 13.sp,
                        color    = ColorTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Schedule,
                        contentDescription = null,
                        modifier           = Modifier.size(14.dp),
                        tint               = ColorTextSecondary
                    )
                    Text(
                        text     = pedido.fechaDisplay,
                        fontSize = 13.sp,
                        color    = ColorTextSecondary
                    )
                }
            }

            // ── Invoice reference (if invoiced) ──────────────────────────────
            if (pedido.factura != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Receipt,
                        contentDescription = null,
                        modifier           = Modifier.size(14.dp),
                        tint               = ColorSuccess
                    )
                    Text(
                        text     = "FAC-%05d".format(pedido.factura.numeroFactura),
                        fontSize = 13.sp,
                        color    = ColorSuccess,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Billing error (inline) ───────────────────────────────────────
            if (hasBillingError) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color  = ColorErrorLight,
                    shape  = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = (billing as BillingAction.Error).message,
                        fontSize = 12.sp,
                        color    = ColorError,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = ColorDivider)
            Spacer(modifier = Modifier.height(10.dp))

            // ── Action buttons ───────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                if (pedido.factura == null) {
                    // Not invoiced: Modificar + Facturar
                    OutlinedButton(
                        onClick  = { showModifyDialog = true },
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Modificar", fontSize = 13.sp)
                    }

                    Spacer(Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        // cobrado == true  → can invoice (green path)
                        // cobrado == false → known unpaid (warning)
                        // cobrado == null  → no data available (neutral lock)
                        val canFacturar = pedido.cobrado == true &&
                                          !isBillingThis &&
                                          billing !is BillingAction.InProgress

                        Button(
                            onClick  = { onFacturar(pedido.pedidoId) },
                            enabled  = canFacturar,
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor         = ColorPrimary,
                                disabledContainerColor = when (pedido.cobrado) {
                                    false -> ColorWarning.copy(alpha = 0.25f)
                                    null  -> ColorNeutralLight
                                    true  -> ColorNeutralLight  // only reached when isBillingThis
                                }
                            )
                        ) {
                            when {
                                isBillingThis -> {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(16.dp),
                                        color       = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Facturando…", fontSize = 13.sp)
                                }
                                pedido.cobrado == false -> {
                                    Icon(
                                        imageVector        = Icons.Filled.Lock,
                                        contentDescription = null,
                                        modifier           = Modifier.size(16.dp),
                                        tint               = ColorWarning
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Pago pendiente", fontSize = 13.sp, color = ColorWarning)
                                }
                                pedido.cobrado == null -> {
                                    Icon(
                                        imageVector        = Icons.Filled.Lock,
                                        contentDescription = null,
                                        modifier           = Modifier.size(16.dp),
                                        tint               = ColorNeutral
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Cobro desconocido", fontSize = 13.sp, color = ColorNeutral)
                                }
                                else -> {
                                    Icon(
                                        imageVector        = Icons.Filled.Description,
                                        contentDescription = null,
                                        modifier           = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Facturar", fontSize = 13.sp)
                                }
                            }
                        }

                        // Hint below the button for blocked states
                        when (pedido.cobrado) {
                            false -> Text(
                                text     = "Completa el cobro para facturar",
                                fontSize = 10.sp,
                                color    = ColorWarning
                            )
                            null  -> Text(
                                text     = "Estado de cobro no disponible",
                                fontSize = 10.sp,
                                color    = ColorNeutral
                            )
                            true  -> { /* no hint needed */ }
                        }
                    }
                } else {
                    // Already invoiced: Compartir (placeholder) + Ver factura
                    OutlinedButton(
                        onClick  = { showShareDialog = true },
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Share,
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Compartir", fontSize = 13.sp)
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick  = { onViewFactura(pedido.factura.facturaId) },
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = ColorSuccess)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Visibility,
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Ver factura", fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (showModifyDialog) {
        AlertDialog(
            onDismissRequest = { showModifyDialog = false },
            icon   = { Icon(Icons.Filled.Construction, contentDescription = null) },
            title  = { Text("Función no disponible") },
            text   = {
                Text(
                    "La edición de pedidos ya facturados estará disponible en una próxima versión.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showModifyDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            icon   = { Icon(Icons.Filled.Share, contentDescription = null) },
            title  = { Text("Compartir factura") },
            text   = {
                Text(
                    "La función de compartir/exportar PDF estará disponible en una próxima versión.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}

// ── Badges ────────────────────────────────────────────────────────────────────

@Composable
private fun EstadoBadge(estado: String) {
    val (bg, fg, label) = when (estado.uppercase()) {
        "CONFIRMADO"  -> Triple(ColorSuccessLight, ColorSuccess,  "Confirmado")
        "PENDIENTE"   -> Triple(ColorWarningLight, ColorWarning,  "Pendiente")
        "CANCELADO"   -> Triple(ColorErrorLight,   ColorError,    "Cancelado")
        else          -> Triple(ColorNeutralLight,  ColorNeutral, estado.lowercase()
            .replaceFirstChar { it.uppercaseChar() })
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(text = label, fontSize = 11.sp, color = fg, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FacturadoBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(ColorPrimaryLight)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(text = "Facturado", fontSize = 11.sp, color = ColorPrimary, fontWeight = FontWeight.Medium)
    }
}

// ── Billing error banner ──────────────────────────────────────────────────────

@Composable
private fun BillingErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        color    = ColorErrorLight,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Error, contentDescription = null, tint = ColorError, modifier = Modifier.size(18.dp))
                Text(message, fontSize = 13.sp, color = ColorError, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = ColorError, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ── Loading / Error / Empty states ────────────────────────────────────────────

@Composable
private fun OrdersLoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = ColorPrimary)
            Spacer(Modifier.height(12.dp))
            Text("Cargando pedidos…", fontSize = 14.sp, color = ColorTextSecondary)
        }
    }
}

@Composable
private fun OrdersErrorContent(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(12.dp),
            modifier              = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector        = Icons.Filled.CloudOff,
                contentDescription = null,
                modifier           = Modifier.size(48.dp),
                tint               = ColorError
            )
            Text(
                text      = message,
                fontSize  = 14.sp,
                color     = ColorTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors  = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun OrdersEmptyContent(filterLast24h: Boolean, onToggle: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(12.dp),
            modifier              = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector        = Icons.Filled.Inbox,
                contentDescription = null,
                modifier           = Modifier.size(56.dp),
                tint               = ColorNeutral
            )
            Text(
                text      = if (filterLast24h)
                    "Sin pedidos en las últimas 24 horas"
                else
                    "No hay pedidos registrados",
                fontSize  = 15.sp,
                fontWeight = FontWeight.Medium,
                color      = ColorTextPrimary,
                textAlign  = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (filterLast24h) {
                Text(
                    text      = "Puedes ver todos los pedidos desactivando el filtro.",
                    fontSize  = 13.sp,
                    color     = ColorTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                OutlinedButton(onClick = onToggle) {
                    Text("Ver todos los pedidos")
                }
            }
        }
    }
}
