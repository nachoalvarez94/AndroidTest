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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.distridulce.model.CartItem
import com.example.distridulce.model.CatalogProduct
import com.example.distridulce.model.OrderSession
import com.example.distridulce.model.OrderableProduct
import com.example.distridulce.model.ProductOption
import com.example.distridulce.model.findClientById
import com.example.distridulce.model.toOrderableProduct
import com.example.distridulce.model.unitLabel
import com.example.distridulce.ui.catalog.CatalogUiState
import com.example.distridulce.ui.catalog.CatalogViewModel
import com.example.distridulce.ui.theme.BackgroundLight
import com.example.distridulce.ui.theme.BrandBlue
import com.example.distridulce.ui.theme.TextPrimary
import com.example.distridulce.ui.theme.TextSecondary

// ── Order builder screen ──────────────────────────────────────────────────────

/**
 * Full-screen split panel:
 *  - Left (60 %) — product catalogue loaded from the backend, with search and
 *    category filters.  Uses [CatalogViewModel] to drive Loading / Error / Success states.
 *  - Right (40 %) — live cart managed by [CartPanel].
 *
 * Products are mapped from [CatalogProduct] to [OrderableProduct] (single "Por Unidades"
 * option) so the existing [AddProductDialog] and cart logic are reused unchanged.
 * [CartItem.articuloId] is populated with the real backend article ID.
 */
@Composable
fun OrderBuilderScreen(
    clientId: String? = null,
    onConfirm: () -> Unit = {},
    onCancel: () -> Unit = {},
    viewModel: CatalogViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Prefer the client already stored in the session (set by NavGraph before
    // navigating here). Fall back to the mock lookup for the standalone catalog
    // entry point where no session client exists.
    val client = remember(clientId) {
        OrderSession.client?.takeIf { it.id == clientId } ?: findClientById(clientId)
    }

    // Products loaded from the backend — kept as CatalogProduct so the left
    // panel can search by codigoInterno / codigoBarras and show unidadVenta.
    // Conversion to OrderableProduct only happens when the user taps "Añadir".
    val catalogProducts = remember(uiState) {
        (uiState as? CatalogUiState.Success)?.products ?: emptyList()
    }

    // ── Cancel confirmation dialog ────────────────────────────────────────────
    var showCancelDialog by remember { mutableStateOf(false) }
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            icon   = { Icon(Icons.Filled.Close, contentDescription = null, tint = Color(0xFFDC2626)) },
            title  = { Text("¿Cancelar el pedido?") },
            text   = {
                Text(
                    "Se descartarán todos los productos añadidos y volverás " +
                    "a la selección de clientes.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        onCancel()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) { Text("Sí, cancelar") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("No, continuar")
                }
            }
        )
    }

    // ── Cart state ────────────────────────────────────────────────────────────
    val cartItems = remember { mutableStateListOf<CartItem>() }
    var dialogProduct by remember { mutableStateOf<CatalogProduct?>(null) }

    // ── Cart operations ───────────────────────────────────────────────────────
    fun addOrIncrement(product: OrderableProduct, option: ProductOption) {
        val key = "${product.id}::${option.id}"
        val idx = cartItems.indexOfFirst { it.key == key }
        if (idx >= 0) {
            cartItems[idx] = cartItems[idx].copy(quantity = cartItems[idx].quantity + 1.0)
        } else {
            cartItems.add(
                CartItem(
                    productId   = product.id,
                    productName = product.name,
                    option      = option,
                    // Carry the real backend article ID so PedidoMapper can use it.
                    articuloId  = product.id.toLongOrNull()
                )
            )
        }
    }

    fun incrementItem(key: String) {
        val idx = cartItems.indexOfFirst { it.key == key }
        if (idx >= 0) cartItems[idx] = cartItems[idx].copy(quantity = cartItems[idx].quantity + 1.0)
    }

    fun decrementItem(key: String) {
        val idx = cartItems.indexOfFirst { it.key == key }
        if (idx >= 0) cartItems[idx] = cartItems[idx].copy(quantity = cartItems[idx].quantity - 1.0)
    }

    fun setQuantity(key: String, qty: Double) {
        val idx = cartItems.indexOfFirst { it.key == key }
        if (idx >= 0) cartItems[idx] = cartItems[idx].copy(quantity = qty)
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
                modifier         = Modifier.weight(0.6f),
                products         = catalogProducts,
                isLoading        = uiState is CatalogUiState.Loading,
                errorMessage     = (uiState as? CatalogUiState.Error)?.message,
                onRetry          = viewModel::loadProducts,
                onAddProduct     = { product -> dialogProduct = product },
                onCancel         = { showCancelDialog = true }
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
                modifier      = Modifier.weight(0.4f),
                client        = client,
                cartItems     = cartItems,
                onIncrement   = { key -> incrementItem(key) },
                onDecrement   = { key -> decrementItem(key) },
                onSetQuantity = { key, qty -> setQuantity(key, qty) },
                onRemove      = { key -> removeItem(key) },
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
    // Convert CatalogProduct → OrderableProduct only here, on demand.
    dialogProduct?.let { catalogProduct ->
        val orderable = catalogProduct.toOrderableProduct()
        AddProductDialog(
            product = orderable,
            onOptionSelected = { option ->
                addOrIncrement(orderable, option)
                dialogProduct = null
            },
            onDismiss = { dialogProduct = null }
        )
    }
}

// ── Product catalogue section (left panel) ────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductCatalogSection(
    modifier: Modifier = Modifier,
    products: List<CatalogProduct>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onAddProduct: (CatalogProduct) -> Unit,
    onCancel: () -> Unit = {}
) {
    val categories = remember(products) {
        listOf("Todos") + products.map { it.category }.distinct().sorted()
    }

    var query           by remember { mutableStateOf("") }
    var selected        by remember { mutableStateOf("Todos") }
    var showBarcodeTip  by remember { mutableStateOf(false) }
    val searchFocus     = remember { FocusRequester() }

    LaunchedEffect(categories) {
        if (selected !in categories) selected = "Todos"
    }

    // Auto-hide the barcode tip after 3 seconds
    LaunchedEffect(showBarcodeTip) {
        if (showBarcodeTip) {
            delay(3_000)
            showBarcodeTip = false
        }
    }

    val filtered = remember(query, selected, products) {
        products.filter { product ->
            val matchesCategory = selected == "Todos" || product.category == selected
            val matchesQuery    = query.isBlank() ||
                product.name.contains(query, ignoreCase = true) ||
                product.description.contains(query, ignoreCase = true) ||
                product.codigoInterno?.contains(query, ignoreCase = true) == true ||
                product.codigoBarras?.contains(query, ignoreCase = true) == true
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Catálogo de productos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cancelar pedido",
                    tint = Color(0xFFDC2626)
                )
            }
        }

        // ── Search bar + barcode button ───────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value         = query,
                onValueChange = { query = it },
                modifier      = Modifier
                    .weight(1f)
                    .focusRequester(searchFocus),
                placeholder   = { Text("Buscar por nombre, ref. o código…", color = TextSecondary) },
                leadingIcon   = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary)
                },
                singleLine = true,
                shape      = RoundedCornerShape(12.dp),
                colors     = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor       = Color.White,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedBorderColor   = BrandBlue
                )
            )
            // Bluetooth barcode reader button — focuses the search field so the
            // scanner (acting as keyboard) writes the barcode directly into it.
            IconButton(
                onClick = {
                    query = ""
                    searchFocus.requestFocus()
                    showBarcodeTip = true
                }
            ) {
                Icon(
                    imageVector        = Icons.Filled.QrCodeScanner,
                    contentDescription = "Lector código de barras",
                    tint               = BrandBlue
                )
            }
        }

        // Barcode reader tip — shown for 3 s after tapping the scanner button
        if (showBarcodeTip) {
            Text(
                text     = "Escanea el código con el lector Bluetooth",
                style    = MaterialTheme.typography.labelSmall,
                color    = BrandBlue,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(BrandBlue.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

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

        // ── Content area: loading / error / grid ──────────────────────────────
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(color = BrandBlue)
                        Text(
                            text  = "Cargando productos…",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint     = Color(0xFFDC2626),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text      = errorMessage,
                            style     = MaterialTheme.typography.bodySmall,
                            color     = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        OutlinedButton(onClick = onRetry) {
                            Text(text = "Reintentar", color = BrandBlue)
                        }
                    }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns               = GridCells.Adaptive(minSize = 180.dp),
                    contentPadding        = PaddingValues(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement   = Arrangement.spacedBy(12.dp),
                    modifier              = Modifier.weight(1f)
                ) {
                    items(items = filtered, key = { it.id }) { product ->
                        ProductOrderCard(
                            product = product,
                            onAdd   = { onAddProduct(product) }
                        )
                    }
                }
            }
        }
    }
}

// ── Product order card ────────────────────────────────────────────────────────

/** Per-category colour helpers. */
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
 * Receives a [CatalogProduct] directly — no prior mapping needed.
 */
@Composable
private fun ProductOrderCard(
    product: CatalogProduct,
    onAdd: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Icon area ─────────────────────────────────────────────────────
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(orderCardIconBg(product.category)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = orderCardIcon(product.category),
                    contentDescription = null,
                    tint               = orderCardIconTint(product.category),
                    modifier           = Modifier.size(32.dp)
                )
            }

            // ── Name + category ───────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text       = product.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    text  = product.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            // ── Price + unit ──────────────────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text       = "€ %.2f".format(product.price),
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color      = BrandBlue
                )
                Text(
                    text  = "/ ${product.unitLabel()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            // ── Add button ────────────────────────────────────────────────────
            Button(
                onClick        = onAdd,
                modifier       = Modifier.fillMaxWidth(),
                shape          = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                colors         = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text(
                    text  = "Añadir",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }
    }
}
