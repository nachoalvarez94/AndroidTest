package com.example.distridulce.ui.orders

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.distridulce.model.CartItem
import com.example.distridulce.model.Client
import com.example.distridulce.ui.theme.BrandBlue
import com.example.distridulce.ui.theme.IconBgBlue
import com.example.distridulce.ui.theme.TextPrimary
import com.example.distridulce.ui.theme.TextSecondary

// ── Quantity format helper ─────────────────────────────────────────────────────

/**
 * Formats a Double quantity for display: whole numbers without decimal point,
 * fractional values with one decimal place.
 * Accessible from sibling files in the same package (e.g. CheckoutScreen).
 */
fun formatQty(qty: Double): String =
    if (qty == kotlin.math.floor(qty) && !qty.isInfinite()) qty.toLong().toString()
    else "%.1f".format(qty)

// ── Cart panel ────────────────────────────────────────────────────────────────

/**
 * Right-side panel of the OrderBuilderScreen.
 * Shows the selected client, a live list of cart items, and the order totals.
 */
@Composable
fun CartPanel(
    modifier: Modifier = Modifier,
    client: Client?,
    cartItems: List<CartItem>,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onSetQuantity: (String, Double) -> Unit,
    onRemove: (String) -> Unit,
    onConfirm: () -> Unit = {}
) {
    val totalItems  = cartItems.sumOf { it.quantity }
    val totalAmount = cartItems.sumOf { it.lineTotal }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CartHeader(client = client)

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

            if (cartItems.isEmpty()) {
                CartEmptyState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(items = cartItems, key = { _, item -> item.key }) { index, item ->
                        CartItemCard(
                            item          = item,
                            onIncrement   = { onIncrement(item.key) },
                            onDecrement   = { onDecrement(item.key) },
                            onSetQuantity = { qty -> onSetQuantity(item.key, qty) },
                            onRemove      = { onRemove(item.key) }
                        )
                        if (index < cartItems.lastIndex) {
                            Divider(
                                modifier = Modifier.padding(vertical = 2.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                        }
                    }
                }
            }

            OrderSummaryPanel(
                enabled     = cartItems.isNotEmpty(),
                totalItems  = totalItems,
                totalAmount = totalAmount,
                onConfirm   = onConfirm
            )
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun CartHeader(client: Client?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .then(
                    if (client != null) Modifier
                    else Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f), CircleShape)
                )
                .let { m -> if (client != null) m else m },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .then(Modifier.size(36.dp))
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Tu Pedido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (client != null) {
                Text(
                    text = client.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandBlue,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun CartEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingCartCheckout,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = TextSecondary.copy(alpha = 0.35f)
            )
            Text(
                text = "El carrito está vacío",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Añade productos desde el catálogo",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary.copy(alpha = 0.7f)
            )
        }
    }
}

// ── Cart item card ────────────────────────────────────────────────────────────

/**
 * One line in the cart: product name + option label, quantity controls,
 * line total, and a delete button.
 */
@Composable
fun CartItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onSetQuantity: (Double) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Product name + option
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.productName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.option.label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        // Quantity controls — editable text field + +/- buttons
        EditableQuantityControl(
            quantity      = item.quantity,
            onIncrement   = onIncrement,
            onDecrement   = onDecrement,
            onSetQuantity = onSetQuantity
        )

        // Line total (negative when quantity is negative → shown as "€-X.XX")
        Text(
            text = "€%.2f".format(item.lineTotal),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (item.lineTotal >= 0) BrandBlue else Color(0xFFDC2626),
            textAlign = TextAlign.End,
            modifier = Modifier.defaultMinSize(minWidth = 56.dp)
        )

        // Delete
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Eliminar",
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ── Editable quantity control ─────────────────────────────────────────────────

/**
 * [-] [editable quantity field] [+] row.
 *
 * - The TextField accepts integers, decimals (comma or period), and negative values.
 * - While focused, intermediate states (empty, "-") are preserved.
 * - On blur: valid values are committed via [onSetQuantity]; invalid input is
 *   restored to the previous [quantity].
 * - Pressing Done on the keyboard clears focus, triggering the blur-commit path.
 * - Pressing [-] or [+] always acts on the committed quantity, even while typing.
 */
@Composable
private fun EditableQuantityControl(
    quantity: Double,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onSetQuantity: (Double) -> Unit
) {
    // rawText holds what is displayed in the field.
    // Re-synced from quantity whenever quantity changes externally AND the field
    // is not focused (i.e. the user is not mid-edit).
    var rawText  by remember { mutableStateOf(formatQty(quantity)) }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(quantity) {
        if (!isFocused) rawText = formatQty(quantity)
    }

    val focusManager = LocalFocusManager.current

    fun commitText() {
        val parsed = rawText.replace(",", ".").toDoubleOrNull()
        isFocused = false
        if (parsed == null || parsed.isNaN() || parsed.isInfinite()) {
            rawText = formatQty(quantity)   // restore — invalid input
        } else {
            onSetQuantity(parsed)
            rawText = formatQty(parsed)     // normalize display (e.g. "3." → "3")
        }
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = "Restar",
                modifier = Modifier.size(13.dp),
                tint = TextSecondary
            )
        }

        BasicTextField(
            value = rawText,
            onValueChange = { new ->
                // Accept empty, lone minus sign, or any partial decimal string.
                if (new.isEmpty() || new == "-" ||
                    new.matches(Regex("-?[0-9]*[.,]?[0-9]*"))) {
                    rawText = new
                }
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                color      = TextPrimary
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction    = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            modifier = Modifier
                .defaultMinSize(minWidth = 44.dp)
                .padding(horizontal = 4.dp)
                .onFocusChanged { state ->
                    if (state.isFocused) {
                        isFocused = true
                    } else if (isFocused) {
                        commitText()
                    }
                }
        )

        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Sumar",
                modifier = Modifier.size(13.dp),
                tint = BrandBlue
            )
        }
    }
}

// ── Order summary panel ───────────────────────────────────────────────────────

/**
 * Totals section pinned at the bottom of the cart panel.
 * Shows total units, total amount, and the confirm button.
 */
@Composable
fun OrderSummaryPanel(
    enabled: Boolean,
    totalItems: Double,
    totalAmount: Double,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total artículos",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = "${formatQty(totalItems)} uds.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total pedido",
                style = MaterialTheme.typography.bodyMedium,
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

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onConfirm,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (enabled) "Confirmar Pedido" else "Añade productos",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
