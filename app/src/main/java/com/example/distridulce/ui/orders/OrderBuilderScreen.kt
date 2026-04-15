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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.distridulce.model.CartItem
import com.example.distridulce.model.OrderSession
import com.example.distridulce.model.OrderableProduct
import com.example.distridulce.model.ProductOption
import com.example.distridulce.model.findClientById
import com.example.distridulce.model.sampleOrderProducts
import com.example.distridulce.ui.theme.BackgroundLight
import com.example.distridulce.ui.theme.BrandBlue
import com.example.distridulce.ui.theme.TextPrimary
import com.example.distridulce.ui.theme.TextSecondary

// ── Order builder screen ──────────────────────────────────────────────────────

/**
 * Full-screen split panel:
 *  - Left (60 %) — product catalogue with search, category filters and a grid of [ProductOrderCard]s.
 *  - Right (40 %) — live cart managed by [CartPanel].
 *
 * All cart state lives here; child composables receive only the data / callbacks they need.
 */
@Composable
fun OrderBuilderScreen(
    clientId: String? = null,
    onConfirm: () -> Unit = {}
) {
    val client = remember(clientId) { findClientById(clientId) }

    // ── Cart state ────────────────────────────────────────────────────────────
    val cartItems = remember { mutableStateListOf<CartItem>() }
    var dialogProduct by remember { mutableStateOf<OrderableProduct?>(null) }

    // ── Cart operations ───────────────────────────────────────────────────────
    fun addOrIncrement(product: OrderableProduct, option: ProductOption) {
        val key = "${product.id}::${option.id}"
        val idx = cartItems.indexOfFirst { it.key == key }
        if (idx >= 0) {
            cartItems[idx] = cartItems[idx].copy(quantity = cartItems[idx].quantity + 1)
        } else {
            cartItems.add(CartItem(product.id, product.name, option))
        }
    }

    fun incrementItem(key: String) {
        val idx = cartItems.indexOfFirst { it.key == key }
        if (idx >= 0) cartItems[idx] = cartItems[idx].copy(quantity = cartItems[idx].quantity + 1)
    }

    fun decrementItem(key: String) {
        val idx = cartItems.indexOfFirst { it.key == key }
        if (idx >= 0) {
            val current = cartItems[idx]
            if (current.quantity > 1) {
                cartItems[idx] = current.copy(quantity = current.quantity - 1)
            } else {
                cartItems.removeAt(idx)
            }
        }
    }

    fun removeItem(key: String) {
        cartItems.removeAll { it.key == key }
    }

    // ── Layout ────────────────────────────────────────────────────────────────
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundLight
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left panel — product catalogue
            ProductCatalogSection(
                modifier = Modifier.weight(0.6f),
                onAddProduct = { product -> dialogProduct = product }
            )

            // Vertical divider
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )

            // Right panel — cart
            CartPanel(
                modifier = Modifier.weight(0.4f),
                client = client,
                cartItems = cartItems,
                onIncrement = { key -> incrementItem(key) },
                onDecrement = { key -> decrementItem(key) },
                onRemove    = { key -> removeItem(key) },
                onConfirm   = {
                    // Snapshot the order into the session before navigating to checkout.
                    OrderSession.clear()
                    OrderSession.client    = client
                    OrderSession.cartItems = cartItems.toList()
                    onConfirm()
                }
            )
        }
    }

    // ── Add-product dialog ────────────────────────────────────────────────────
    dialogProduct?.let { product ->
        AddProductDialog(
            product = product,
            onOptionSelected = { option ->
                addOrIncrement(product, option)
                dialogProduct = null
            },
            onDismiss = { dialogProduct = null }
        )
    }
}

// ── Product catalogue section (left panel) ────────────────────────────────────

private val categories = listOf("Todos", "Galletas", "Bollería", "Dulces")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductCatalogSection(
    modifier: Modifier = Modifier,
    onAddProduct: (OrderableProduct) -> Unit
) {
    var query    by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("Todos") }

    val filtered = remember(query, selected) {
        sampleOrderProducts.filter { product ->
            val matchesCategory = selected == "Todos" || product.category == selected
            val matchesQuery    = query.isBlank() ||
                product.name.contains(query, ignoreCase = true) ||
                product.category.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Text(
            text = "Catálogo de productos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // ── Search bar ────────────────────────────────────────────────────────
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Buscar producto…", color = TextSecondary)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = TextSecondary
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color.White,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedBorderColor = BrandBlue
            )
        )

        // ── Category filter chips ─────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { category ->
                FilterChip(
                    selected = selected == category,
                    onClick  = { selected = category },
                    label    = { Text(category) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandBlue,
                        selectedLabelColor     = Color.White
                    )
                )
            }
        }

        // ── Product grid ──────────────────────────────────────────────────────
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement   = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(items = filtered, key = { it.id }) { product ->
                ProductOrderCard(
                    product  = product,
                    onAdd    = { onAddProduct(product) }
                )
            }
        }
    }
}

// ── Product order card ────────────────────────────────────────────────────────

/** Per-category colour helpers (mirrors AddProductDialog helpers, kept local). */
private fun orderCardIconBg(category: String): Color = when (category) {
    "Galletas" -> Color(0xFFFEF3C7)
    "Bollería" -> Color(0xFFFEE2E2)
    "Dulces"   -> Color(0xFFEDE9FE)
    else       -> Color(0xFFDCEBFD)
}

private fun orderCardIconTint(category: String): Color = when (category) {
    "Galletas" -> Color(0xFFD97706)
    "Bollería" -> Color(0xFFDC2626)
    "Dulces"   -> Color(0xFF7C3AED)
    else       -> Color(0xFF2563EB)
}

private fun orderCardIcon(category: String): ImageVector = when (category) {
    "Galletas" -> Icons.Filled.LocalGroceryStore
    "Bollería" -> Icons.Filled.Fastfood
    "Dulces"   -> Icons.Filled.Cake
    else       -> Icons.Filled.Inventory2
}

/**
 * Compact product card used inside the order-builder grid.
 * Shows the product icon, name, category, starting price and an "Añadir" button.
 */
@Composable
private fun ProductOrderCard(
    product: OrderableProduct,
    onAdd: () -> Unit
) {
    val minPrice = product.options.minOf { it.price }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Icon area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(orderCardIconBg(product.category)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = orderCardIcon(product.category),
                    contentDescription = null,
                    tint = orderCardIconTint(product.category),
                    modifier = Modifier.size(32.dp)
                )
            }

            // Name + category
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            // Starting price
            Text(
                text = "desde €%.2f".format(minPrice),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = BrandBlue
            )

            // Add button
            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text(
                    text = "Añadir",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }
    }
}
