@file:OptIn(ExperimentalComposeUiApi::class)

package com.example.distridulce.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.distridulce.model.OrderableProduct
import com.example.distridulce.model.ProductOption
import com.example.distridulce.ui.theme.BackgroundLight
import com.example.distridulce.ui.theme.BrandBlue
import com.example.distridulce.ui.theme.TextPrimary
import com.example.distridulce.ui.theme.TextSecondary

// ── Category helpers (local) ──────────────────────────────────────────────────

private fun dialogIconBg(category: String): Color = when (category) {
    "Galletas" -> Color(0xFFFEF3C7)
    "Bollería" -> Color(0xFFFEE2E2)
    "Dulces"   -> Color(0xFFEDE9FE)
    else       -> Color(0xFFDCEBFD)
}

private fun dialogIconTint(category: String): Color = when (category) {
    "Galletas" -> Color(0xFFD97706)
    "Bollería" -> Color(0xFFDC2626)
    "Dulces"   -> Color(0xFF7C3AED)
    else       -> Color(0xFF2563EB)
}

private fun dialogIcon(category: String): ImageVector = when (category) {
    "Galletas" -> Icons.Filled.LocalGroceryStore
    "Bollería" -> Icons.Filled.Fastfood
    "Dulces"   -> Icons.Filled.Cake
    else       -> Icons.Filled.Inventory2
}

// ── Dialog ────────────────────────────────────────────────────────────────────

/**
 * Modal shown when the user taps "Añadir" on a product.
 * Displays all [OrderableProduct.options]; clicking one calls [onOptionSelected].
 */
@Composable
fun AddProductDialog(
    product: OrderableProduct,
    onOptionSelected: (ProductOption) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.width(460.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Header ────────────────────────────────────────────────────
                DialogHeader(product = product)

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // ── Option label ──────────────────────────────────────────────
                Text(
                    text = "Selecciona una modalidad",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                // ── Options ───────────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    product.options.forEach { option ->
                        ProductOptionCard(
                            option = option,
                            onClick = { onOptionSelected(option) }
                        )
                    }
                }

                // ── Cancel ────────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = TextSecondary)
                    }
                }
            }
        }
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun DialogHeader(product: OrderableProduct) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(dialogIconBg(product.category)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = dialogIcon(product.category),
                contentDescription = null,
                tint = dialogIconTint(product.category),
                modifier = Modifier.size(22.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Añadir al pedido",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

/**
 * A single tappable row for one purchase option.
 * Shows label + description on the left, price on the right.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductOptionCard(option: ProductOption, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "€ %.2f".format(option.price),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue
                )
                Text(
                    text = "por ${option.unit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}
