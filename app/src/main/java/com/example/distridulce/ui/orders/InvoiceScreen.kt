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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import com.example.distridulce.model.CartItem
import com.example.distridulce.model.OrderSession
import com.example.distridulce.model.PaymentSummary
import com.example.distridulce.ui.theme.BackgroundLight
import com.example.distridulce.ui.theme.BrandBlue
import com.example.distridulce.ui.theme.TextPrimary
import com.example.distridulce.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Invoice screen ────────────────────────────────────────────────────────────

/**
 * Final step of the order flow: displays the generated invoice/factura.
 *
 * Reads all data from [OrderSession] — no nav arguments needed.
 * [onNewOrder] should navigate back to NewOrderScreen and clear the session.
 */
@Composable
fun InvoiceScreen(onNewOrder: () -> Unit = {}) {
    val session  = OrderSession
    val summary  = session.paymentSummary ?: return
    val dateStr  = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES")).format(Date())
    }

    Surface(modifier = Modifier.fillMaxSize(), color = BackgroundLight) {
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
                // ── Status header ─────────────────────────────────────────────
                InvoiceStatusHeader(summary = summary)

                // ── Invoice card ──────────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(20.dp),
                    colors   = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(28.dp)) {

                        // Document header
                        InvoiceDocumentHeader(ref = session.invoiceRef, date = dateStr)

                        Spacer(Modifier.height(20.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(Modifier.height(16.dp))

                        // Client section
                        session.client?.let { client ->
                            InvoiceClientSection(clientName = client.name, address = client.address)
                            Spacer(Modifier.height(20.dp))
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(Modifier.height(16.dp))
                        }

                        // Products table
                        InvoiceProductsTable(cartItems = session.cartItems)

                        Spacer(Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(Modifier.height(16.dp))

                        // Payment summary
                        InvoiceSummarySection(summary = summary)
                    }
                }

                // ── Action buttons ────────────────────────────────────────────
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
                        Text("Compartir factura")
                    }

                    Button(
                        onClick  = {
                            OrderSession.clear()
                            onNewOrder()
                        },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = BrandBlue)
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
}

// ── Status header ─────────────────────────────────────────────────────────────

@Composable
private fun InvoiceStatusHeader(summary: PaymentSummary) {
    val (icon, iconBg, iconTint, label) = when (summary.paymentStatus) {
        "Pagado Completo" -> StatusVisuals(
            Icons.Filled.CheckCircle,
            Color(0xFFD1FAE5), Color(0xFF059669),
            "Pagado completo"
        )
        "Pago Parcial"    -> StatusVisuals(
            Icons.Filled.Tune,
            Color(0xFFDCEBFD), BrandBlue,
            "Pago parcial registrado"
        )
        else              -> StatusVisuals(
            Icons.Filled.Schedule,
            Color(0xFFFEF3C7), Color(0xFFD97706),
            "Pago pendiente"
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
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp))
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
private fun InvoiceClientSection(clientName: String, address: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text  = "FACTURAR A",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            letterSpacing = androidx.compose.ui.unit.TextUnit(1.5f, androidx.compose.ui.unit.TextUnitType.Sp)
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
    }
}

// ── Products table ────────────────────────────────────────────────────────────

@Composable
private fun InvoiceProductsTable(cartItems: List<CartItem>) {
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
                text = "Ud.",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.width(48.dp),
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
        cartItems.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text  = item.productName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text  = item.option.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                Text(
                    text  = "${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(48.dp)
                )
                Text(
                    text  = "€%.2f".format(item.option.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(64.dp)
                )
                Text(
                    text  = "€%.2f".format(item.lineTotal),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(72.dp)
                )
            }
            if (index < cartItems.lastIndex) {
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
 * The payment totals block at the bottom of the invoice.
 * Adapts to all three payment states (full / partial / pending).
 */
@Composable
fun InvoiceSummarySection(
    summary: PaymentSummary,
    modifier: Modifier = Modifier
) {
    // Payment status badge colours
    val (statusBg, statusFg) = when (summary.paymentStatus) {
        "Pagado Completo" -> Color(0xFFD1FAE5) to Color(0xFF059669)
        "Pago Parcial"    -> Color(0xFFDCEBFD) to BrandBlue
        else              -> Color(0xFFFEF3C7) to Color(0xFFD97706)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Subtotal
        SummaryRow(label = "Subtotal", value = "€ %.2f".format(summary.totalAmount))

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

        // Importe abonado
        SummaryRow(
            label     = "Importe abonado",
            value     = "€ %.2f".format(summary.paidAmount),
            valueColor = if (summary.paidAmount > 0) Color(0xFF059669) else TextSecondary
        )

        // Importe pendiente
        SummaryRow(
            label     = "Importe pendiente",
            value     = "€ %.2f".format(summary.pendingAmount),
            valueColor = if (summary.pendingAmount > 0) Color(0xFFD97706) else TextSecondary
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
                    text  = summary.paymentStatus,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = statusFg
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        Spacer(Modifier.height(4.dp))

        // TOTAL — prominent
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
                text  = "€ %.2f".format(summary.totalAmount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = BrandBlue
            )
        }
    }
}

// ── Shared row ────────────────────────────────────────────────────────────────

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
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
