@file:OptIn(ExperimentalUnitApi::class)

package com.example.distridulce.ui.orders

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.distridulce.model.OrderSession
import com.example.distridulce.network.dto.FacturaLineaResponseDto
import com.example.distridulce.network.dto.FacturaResponseDto
import com.example.distridulce.ui.theme.BackgroundLight
import com.example.distridulce.ui.theme.BrandBlue
import com.example.distridulce.ui.theme.TextPrimary
import com.example.distridulce.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale

// ── Invoice screen ────────────────────────────────────────────────────────────

/**
 * Final step of the order flow: generates and displays the real invoice.
 *
 * [InvoiceViewModel] calls POST /api/facturas/desde-pedido/{pedidoId} on first
 * open and caches the result in [OrderSession] to prevent duplicate creation
 * on revisit.  Payment status is read from the associated Pedido via a second
 * GET request — FacturaResponseDto does not carry estadoCobro / importeCobrado.
 */
@Composable
fun InvoiceScreen(
    onBack: () -> Unit = {},
    onNewOrder: () -> Unit = {},
    viewModel: InvoiceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = BackgroundLight) {
        // Back button overlay — always accessible regardless of load state
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is InvoiceUiState.Loading -> InvoiceLoadingContent()

                is InvoiceUiState.Error -> InvoiceErrorContent(
                    message = state.message,
                    onRetry = viewModel::generateFactura,
                    onBack  = onBack
                )

                is InvoiceUiState.Success -> InvoiceSuccessContent(
                    factura          = state.factura,
                    estadoCobro      = state.estadoCobro,
                    importeCobrado   = state.importeCobrado,
                    importePendiente = state.importePendiente,
                    onBack           = onBack,
                    onNewOrder       = {
                        OrderSession.clear()
                        onNewOrder()
                    }
                )
            }

            // Floating back arrow — visible in all states (including Loading)
            IconButton(
                onClick  = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Filled.ArrowBack,
                    contentDescription = "Volver a Pedidos",
                    tint               = TextSecondary
                )
            }
        }
    }
}

// ── Loading content ───────────────────────────────────────────────────────────

@Composable
private fun InvoiceLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = BrandBlue, modifier = Modifier.size(48.dp))
            Text(
                text  = "Generando factura…",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        }
    }
}

// ── Error content ─────────────────────────────────────────────────────────────

@Composable
private fun InvoiceErrorContent(message: String, onRetry: () -> Unit, onBack: () -> Unit = {}) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(56.dp)
            )
            Text(
                text  = "No se pudo generar la factura",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text  = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            OutlinedButton(onClick = onRetry) {
                Text(text = "Reintentar", color = BrandBlue)
            }
        }
    }
}

// ── Success content ───────────────────────────────────────────────────────────

@Composable
private fun InvoiceSuccessContent(
    factura: FacturaResponseDto,
    estadoCobro: String?,
    importeCobrado: Double?,
    importePendiente: Double?,
    onBack: () -> Unit = {},
    onNewOrder: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .padding(horizontal = 32.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Status header ─────────────────────────────────────────────────
            InvoiceStatusHeader(estadoCobro = estadoCobro)

            // ── Invoice card ──────────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(28.dp)) {

                    // Document header — real invoice number from backend
                    InvoiceDocumentHeader(
                        ref  = "FAC-%05d".format(factura.numeroFactura),
                        date = formatFechaEmision(factura.fechaEmision)
                    )

                    Spacer(Modifier.height(20.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(Modifier.height(16.dp))

                    // Client section — from backend's client record
                    InvoiceClientSection(
                        clientName      = factura.nombreCliente,
                        address         = factura.direccionCliente,
                        documentoFiscal = factura.documentoFiscalCliente
                    )

                    Spacer(Modifier.height(20.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(Modifier.height(16.dp))

                    // Products table — backend invoice lines (includes IVA data)
                    InvoiceProductsTable(lineas = factura.lineas)

                    Spacer(Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(Modifier.height(16.dp))

                    // Totals + payment summary
                    InvoiceSummarySection(
                        estadoCobro      = estadoCobro,
                        importeCobrado   = importeCobrado,
                        importePendiente = importePendiente,
                        baseImponible    = factura.baseImponible,
                        impuestos        = factura.impuestos,
                        totalFinal       = factura.total
                    )
                }
            }

            // ── Action buttons ────────────────────────────────────────────────
            // Primary: back to orders list
            Button(
                onClick  = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Volver a Pedidos")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = { /* TODO: share / print */ },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Compartir")
                }

                OutlinedButton(
                    onClick  = onNewOrder,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Nuevo pedido")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Status header ─────────────────────────────────────────────────────────────

@Composable
private fun InvoiceStatusHeader(estadoCobro: String?) {
    val (icon, iconBg, iconTint, label) = when (estadoCobro) {
        "COMPLETO"  -> StatusVisuals(
            Icons.Filled.CheckCircle,
            Color(0xFFD1FAE5), Color(0xFF059669),
            "Pagado completo"
        )
        "PARCIAL"   -> StatusVisuals(
            Icons.Filled.Tune,
            Color(0xFFDCEBFD), BrandBlue,
            "Pago parcial registrado"
        )
        "PENDIENTE" -> StatusVisuals(
            Icons.Filled.Schedule,
            Color(0xFFFEF3C7), Color(0xFFD97706),
            "Pago pendiente"
        )
        else        -> StatusVisuals(
            Icons.Filled.Schedule,
            Color(0xFFECEFF1), Color(0xFF546E7A),
            "Estado de cobro no disponible"
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text  = "Factura generada",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = iconTint,
            fontWeight = FontWeight.Medium
        )
    }
}

/** Simple data holder for status visuals — avoids a destructure mismatch. */
private data class StatusVisuals(
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val label: String
)

// ── Document header ───────────────────────────────────────────────────────────

@Composable
private fun InvoiceDocumentHeader(ref: String, date: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text  = "FACTURA",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BrandBlue
            )
            Text(
                text  = "DistriDulce S.L.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text  = ref,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text  = date,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

// ── Client section ────────────────────────────────────────────────────────────

@Composable
private fun InvoiceClientSection(
    clientName: String,
    address: String,
    documentoFiscal: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text  = "FACTURAR A",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            letterSpacing = TextUnit(1.5f, TextUnitType.Sp)
        )
        Text(
            text  = clientName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Text(
            text  = address,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        if (!documentoFiscal.isNullOrBlank()) {
            Text(
                text  = documentoFiscal,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

// ── Products table ────────────────────────────────────────────────────────────

/**
 * Renders the invoice line table from the backend [FacturaLineaResponseDto] list.
 * Columns: Producto | Cant. | Precio | Total
 */
@Composable
private fun InvoiceProductsTable(lineas: List<FacturaLineaResponseDto>) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        // Table header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundLight, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Producto",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Cant.",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.width(52.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Precio",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.width(64.dp),
                textAlign = TextAlign.End
            )
            Text(
                text = "Total",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.width(72.dp),
                textAlign = TextAlign.End
            )
        }

        // Table rows
        lineas.forEachIndexed { index, linea ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text  = linea.nombreArticulo,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    // Show article code if available (e.g. internal ref or barcode)
                    if (!linea.codigoArticulo.isNullOrBlank()) {
                        Text(
                            text  = linea.codigoArticulo,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    // IVA rate as a subtle label
                    Text(
                        text  = "IVA ${linea.tipoIva.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                Text(
                    text  = formatCantidad(linea.cantidad),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(52.dp)
                )
                Text(
                    text  = "€%.2f".format(linea.precioUnitario),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(64.dp)
                )
                Text(
                    text  = "€%.2f".format(linea.totalLinea),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(72.dp)
                )
            }
            if (index < lineas.lastIndex) {
                Divider(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
            }
        }
    }
}

// ── Invoice summary section ───────────────────────────────────────────────────

/**
 * The fiscal totals + payment state block at the bottom of the invoice.
 *
 * Payment fields come from the Pedido associated with this invoice — the Factura
 * DTO does not carry them.  All three are nullable for legacy orders.
 *
 * @param estadoCobro      "COMPLETO", "PARCIAL", "PENDIENTE", or null.
 * @param importeCobrado   Amount actually collected; null for legacy records.
 * @param importePendiente Remaining balance; null for legacy records.
 * @param baseImponible    Tax base from the backend.
 * @param impuestos        Tax amount from the backend.
 * @param totalFinal       Grand total from the backend (authoritative figure).
 */
@Composable
fun InvoiceSummarySection(
    estadoCobro: String?,
    importeCobrado: Double?,
    importePendiente: Double?,
    baseImponible: Double,
    impuestos: Double,
    totalFinal: Double,
    modifier: Modifier = Modifier
) {
    val (statusBg, statusFg, statusLabel) = when (estadoCobro) {
        "COMPLETO"  -> Triple(Color(0xFFD1FAE5), Color(0xFF059669), "Pagado completo")
        "PARCIAL"   -> Triple(Color(0xFFDCEBFD), BrandBlue,         "Pago parcial")
        "PENDIENTE" -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "Pago pendiente")
        else        -> Triple(Color(0xFFECEFF1), Color(0xFF546E7A), "Desconocido")
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Fiscal breakdown (from backend)
        SummaryRow(label = "Base imponible", value = "€ %.2f".format(baseImponible))
        SummaryRow(label = "IVA",            value = "€ %.2f".format(impuestos))

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

        // Payment breakdown (from the associated Pedido)
        SummaryRow(
            label      = "Importe abonado",
            value      = importeCobrado?.let { "€ %.2f".format(it) } ?: "—",
            valueColor = when {
                importeCobrado == null -> TextSecondary
                importeCobrado > 0    -> Color(0xFF059669)
                else                  -> TextSecondary
            }
        )
        SummaryRow(
            label      = "Importe pendiente",
            value      = importePendiente?.let { "€ %.2f".format(it) } ?: "—",
            valueColor = when {
                importePendiente == null  -> TextSecondary
                importePendiente > 0.0   -> Color(0xFFD97706)
                else                     -> TextSecondary
            }
        )

        // Estado de pago badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text  = "Estado de pago",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(statusBg)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text  = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = statusFg
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        Spacer(Modifier.height(4.dp))

        // TOTAL — prominent, authoritative figure from backend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text  = "TOTAL",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text  = "€ %.2f".format(totalFinal),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = BrandBlue
            )
        }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    labelColor: Color = TextSecondary,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = labelColor)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

/**
 * Formats a [FacturaResponseDto.fechaEmision] string (ISO 8601 date or datetime)
 * into a human-readable Spanish locale string.
 */
private fun formatFechaEmision(fechaEmision: String): String {
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss" to "dd/MM/yyyy HH:mm",
        "yyyy-MM-dd'T'HH:mm:ss.SSS" to "dd/MM/yyyy HH:mm",
        "yyyy-MM-dd" to "dd/MM/yyyy"
    )
    val locale = Locale("es", "ES")
    for ((inputPattern, outputPattern) in formats) {
        try {
            val parsed = SimpleDateFormat(inputPattern, locale).parse(fechaEmision)
            if (parsed != null) {
                return SimpleDateFormat(outputPattern, locale).format(parsed)
            }
        } catch (_: Exception) { /* try next */ }
    }
    return fechaEmision  // fallback: return raw string unchanged
}

/**
 * Formats a [Double] quantity as a clean integer string when the value is
 * whole ("3.0" → "3"), or as two decimal places otherwise ("1.5" → "1.50").
 */
private fun formatCantidad(cantidad: Double): String =
    if (cantidad == kotlin.math.floor(cantidad)) {
        cantidad.toLong().toString()
    } else {
        "%.2f".format(cantidad)
    }
