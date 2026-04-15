package com.example.distridulce.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.distridulce.model.CartItem
import com.example.distridulce.model.OrderSession
import com.example.distridulce.model.PaymentMethod
import com.example.distridulce.model.PaymentSummary
import com.example.distridulce.ui.theme.BackgroundLight
import com.example.distridulce.ui.theme.BrandBlue
import com.example.distridulce.ui.theme.TextPrimary
import com.example.distridulce.ui.theme.TextSecondary

// ── Checkout screen ───────────────────────────────────────────────────────────

/**
 * Step 3 of the order flow: choose a payment method, optionally enter a partial
 * amount, then confirm to generate the invoice.
 *
 * Layout: left panel (order review) + right panel (payment selection).
 */
@Composable
fun CheckoutScreen(
    onConfirm: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val session     = OrderSession
    val totalAmount = remember { session.cartItems.sumOf { it.lineTotal } }

    // ── Payment state ─────────────────────────────────────────────────────────
    var selectedMethod      by remember { mutableStateOf<PaymentMethod?>(null) }
    var partialAmountText   by remember { mutableStateOf("") }

    // Validate partial input — only evaluated when method is PARTIAL
    val partialAmount: Double? by remember(partialAmountText) {
        derivedStateOf { partialAmountText.replace(",", ".").toDoubleOrNull() }
    }

    val partialError: String? by remember(selectedMethod, partialAmount, totalAmount) {
        derivedStateOf {
            if (selectedMethod != PaymentMethod.PARTIAL) return@derivedStateOf null
            when {
                partialAmountText.isBlank()      -> null // not yet touched
                partialAmount == null            -> "Introduce un número válido"
                partialAmount!! <= 0.0           -> "El importe debe ser mayor que 0"
                partialAmount!! >= totalAmount   -> "El importe debe ser menor que el total (usa Pago Completo)"
                else                             -> null
            }
        }
    }

    val canConfirm by remember(selectedMethod, partialAmountText, partialAmount, partialError) {
        derivedStateOf {
            when (selectedMethod) {
                PaymentMethod.FULL, PaymentMethod.PENDING -> true
                PaymentMethod.PARTIAL ->
                    partialAmount != null &&
                    partialAmount!! > 0.0 &&
                    partialAmount!! < totalAmount &&
                    partialError == null
                null -> false
            }
        }
    }

    // ── Build payment summary and navigate ────────────────────────────────────
    fun confirm() {
        val summary = when (selectedMethod) {
            PaymentMethod.FULL    -> PaymentSummary.full(totalAmount)
            PaymentMethod.PARTIAL -> PaymentSummary.partial(totalAmount, partialAmount!!)
            PaymentMethod.PENDING -> PaymentSummary.pending(totalAmount)
            null                  -> return
        }
        session.paymentSummary = summary
        session.invoiceRef     = "INV-${(100000..999999).random()}"
        onConfirm()
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Surface(modifier = Modifier.fillMaxSize(), color = BackgroundLight) {
        Row(modifier = Modifier.fillMaxSize()) {

            // ── Left: order review ────────────────────────────────────────────
            OrderReviewPanel(
                modifier    = Modifier.weight(0.55f),
                cartItems   = session.cartItems,
                totalAmount = totalAmount,
                onBack      = onBack
            )

            // Vertical divider
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )

            // ── Right: payment selection ──────────────────────────────────────
            PaymentPanel(
                modifier            = Modifier.weight(0.45f),
                totalAmount         = totalAmount,
                selectedMethod      = selectedMethod,
                partialAmountText   = partialAmountText,
                partialError        = partialError,
                canConfirm          = canConfirm,
                onMethodSelected    = { method ->
                    selectedMethod    = method
                    partialAmountText = ""   // reset input on method change
                },
                onPartialAmountChange = { partialAmountText = it },
                onConfirm           = { confirm() }
            )
        }
    }
}

// ── Order review panel (left) ─────────────────────────────────────────────────

@Composable
private fun OrderReviewPanel(
    modifier: Modifier = Modifier,
    cartItems: List<CartItem>,
    totalAmount: Double,
    onBack: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header row
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextSecondary
                )
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = "Resumen del pedido",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                OrderSession.client?.let { client ->
                    Text(
                        text = client.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Item list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            itemsIndexed(items = cartItems, key = { _, item -> item.key }) { index, item ->
                ReviewItemRow(item = item)
                if (index < cartItems.lastIndex) {
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    )
                }
            }
        }

        // Total footer
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total del pedido",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "€ %.2f".format(totalAmount),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BrandBlue
            )
        }
    }
}

@Composable
private fun ReviewItemRow(item: CartItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = item.productName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "${item.option.label} · ×${item.quantity}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        Text(
            text = "€%.2f".format(item.lineTotal),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = BrandBlue,
            textAlign = TextAlign.End
        )
    }
}

// ── Payment panel (right) ─────────────────────────────────────────────────────

@Composable
private fun PaymentPanel(
    modifier: Modifier = Modifier,
    totalAmount: Double,
    selectedMethod: PaymentMethod?,
    partialAmountText: String,
    partialError: String?,
    canConfirm: Boolean,
    onMethodSelected: (PaymentMethod) -> Unit,
    onPartialAmountChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Método de pago",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Selecciona cómo se abona este pedido",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(Modifier.height(4.dp))

        // ── Payment method cards ──────────────────────────────────────────────
        PaymentMethodCard(
            icon        = Icons.Filled.CheckCircle,
            iconBgColor = Color(0xFFD1FAE5),
            iconTint    = Color(0xFF059669),
            title       = "Pago Completo",
            description = "El cliente abona el importe total ahora",
            selected    = selectedMethod == PaymentMethod.FULL,
            onClick     = { onMethodSelected(PaymentMethod.FULL) }
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PaymentMethodCard(
                icon        = Icons.Filled.Tune,
                iconBgColor = Color(0xFFDCEBFD),
                iconTint    = BrandBlue,
                title       = "Pago Parcial",
                description = "El cliente adelanta una parte del importe",
                selected    = selectedMethod == PaymentMethod.PARTIAL,
                onClick     = { onMethodSelected(PaymentMethod.PARTIAL) }
            )

            // Partial payment input — visible only when PARTIAL is selected
            if (selectedMethod == PaymentMethod.PARTIAL) {
                PartialPaymentInput(
                    value       = partialAmountText,
                    onValueChange = onPartialAmountChange,
                    totalAmount = totalAmount,
                    error       = partialError
                )
            }
        }

        PaymentMethodCard(
            icon        = Icons.Filled.Schedule,
            iconBgColor = Color(0xFFFEF3C7),
            iconTint    = Color(0xFFD97706),
            title       = "Pago Pendiente",
            description = "El pedido se sirve; el pago queda pendiente",
            selected    = selectedMethod == PaymentMethod.PENDING,
            onClick     = { onMethodSelected(PaymentMethod.PENDING) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // ── Confirm button ────────────────────────────────────────────────────
        Button(
            onClick  = onConfirm,
            enabled  = canConfirm,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = BrandBlue)
        ) {
            Icon(
                imageVector = Icons.Filled.Receipt,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text  = "Confirmar y Generar Factura",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

// ── Payment method card ───────────────────────────────────────────────────────

/**
 * Selectable card representing a single payment method.
 * When [selected], the card shows a coloured border and a light tinted background.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodCard(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) iconTint else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val bgColor     = if (selected) iconBgColor.copy(alpha = 0.35f) else Color.White

    Card(
        onClick  = onClick,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            ),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ── Partial payment input ─────────────────────────────────────────────────────

/**
 * Numeric text field for entering a partial payment amount.
 * Shows a € prefix, validates inline, and surfaces [error] below the field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartialPaymentInput(
    value: String,
    onValueChange: (String) -> Unit,
    totalAmount: Double,
    error: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text  = "Cantidad abonada",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )

        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            modifier      = Modifier.fillMaxWidth(),
            placeholder   = { Text("Introduce el importe pagado", color = TextSecondary) },
            leadingIcon   = {
                Text(
                    text  = "€",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (error != null) Color(0xFFDC2626) else BrandBlue,
                    modifier = Modifier.padding(start = 4.dp)
                )
            },
            singleLine    = true,
            isError       = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape         = RoundedCornerShape(10.dp),
            colors        = TextFieldDefaults.outlinedTextFieldColors(
                containerColor      = Color(0xFFF9FAFB),
                focusedBorderColor  = BrandBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                errorBorderColor    = Color(0xFFDC2626)
            )
        )

        // Error message
        if (error != null) {
            Text(
                text  = error,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFDC2626)
            )
        } else {
            // Helper text showing max allowed
            Text(
                text  = "Máximo: € %.2f".format(totalAmount),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
