package com.example.distridulce.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.distridulce.model.CatalogProduct
import com.example.distridulce.model.findClientById
import com.example.distridulce.ui.theme.BrandBlue
import com.example.distridulce.ui.theme.IconBgBlue
import com.example.distridulce.ui.theme.TextPrimary
import com.example.distridulce.ui.theme.TextSecondary

// ── Category visual styles ────────────────────────────────────────────────────

private fun categoryIconBg(category: String): Color = when (category) {
    "Galletas" -> Color(0xFFFEF3C7)
    "Bollería" -> Color(0xFFFEE2E2)
    "Dulces"   -> Color(0xFFEDE9FE)
    else       -> Color(0xFFDCEBFD)
}

private fun categoryIconTint(category: String): Color = when (category) {
    "Galletas" -> Color(0xFFD97706)
    "Bollería" -> Color(0xFFDC2626)
    "Dulces"   -> Color(0xFF7C3AED)
    else       -> Color(0xFF2563EB)
}

private fun categoryBadgeBg(category: String): Color = when (category) {
    "Galletas" -> Color(0xFFFEF3C7)
    "Bollería" -> Color(0xFFFEE2E2)
    "Dulces"   -> Color(0xFFEDE9FE)
    else       -> Color(0xFFDCEBFD)
}

private fun categoryBadgeText(category: String): Color = when (category) {
    "Galletas" -> Color(0xFFB45309)
    "Bollería" -> Color(0xFFB91C1C)
    "Dulces"   -> Color(0xFF6D28D9)
    else       -> Color(0xFF1D4ED8)
}

private fun categoryIcon(category: String): ImageVector = when (category) {
    "Galletas" -> Icons.Filled.LocalGroceryStore
    "Bollería" -> Icons.Filled.Fastfood
    "Dulces"   -> Icons.Filled.Cake
    else       -> Icons.Filled.Inventory2
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * @param clientId When non-null the screen was opened from NewOrderScreen.
 *                 A context banner is shown so the user knows which client
 *                 they are building the order for.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    clientId: String? = null,
    viewModel: CatalogViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val client = remember(clientId) { findClientById(clientId) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CatalogHeader()

            // Show ordering context when coming from NewOrderScreen
            if (client != null) {
                ClientContextBanner(clientName = client.name)
            }

            when (val state = uiState) {
                is CatalogUiState.Loading -> {
                    CatalogLoadingState(modifier = Modifier.weight(1f))
                }

                is CatalogUiState.Error -> {
                    CatalogErrorState(
                        message = state.message,
                        onRetry = viewModel::loadProducts,
                        modifier = Modifier.weight(1f)
                    )
                }

                is CatalogUiState.Success -> {
                    CatalogContent(
                        products = state.products,
                        client   = client
                    )
                }
            }
        }
    }
}

// ── Loaded content ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogContent(
    products: List<CatalogProduct>,
    client: com.example.distridulce.model.Client?
) {
    // Derive categories dynamically from the loaded data
    val categories = remember(products) {
        listOf("Todos") + products.map { it.category }.distinct().sorted()
    }

    var searchQuery     by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    // If a data refresh changes the available categories, reset the filter
    LaunchedEffect(categories) {
        if (selectedCategory !in categories) selectedCategory = "Todos"
    }

    val filteredProducts = remember(searchQuery, selectedCategory, products) {
        products.filter { product ->
            val matchesSearch = product.name.contains(searchQuery, ignoreCase = true) ||
                    product.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "Todos" ||
                    product.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    CatalogSearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
    CatalogFilterRow(
        categories       = categories,
        selectedCategory = selectedCategory,
        onCategorySelected = { selectedCategory = it }
    )
    ProductCountLabel(count = filteredProducts.size)

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 220.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = filteredProducts, key = { it.id }) { product ->
            ProductCard(
                name        = product.name,
                description = product.description,
                category    = product.category,
                price       = product.price,
                stock       = product.stock
            )
        }
    }
}

// ── Loading state ─────────────────────────────────────────────────────────────

@Composable
private fun CatalogLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(color = BrandBlue)
            Text(
                text = "Cargando catálogo…",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

// ── Error state ───────────────────────────────────────────────────────────────

@Composable
private fun CatalogErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
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

// ── Client context banner ─────────────────────────────────────────────────────

/**
 * Shown when the catalog is opened from NewOrderScreen.
 * Reminds the user which client they are building the order for.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientContextBanner(clientName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = IconBgBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier.size(18.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = "Creando pedido para",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = clientName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandBlue
                )
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun CatalogHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Catálogo de Productos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Explora todos los productos disponibles",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

// ── Search bar ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Buscar productos...",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Buscar",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = Color.White,
            unfocusedBorderColor = Color(0xFFE5E7EB),
            focusedBorderColor = BrandBlue,
        )
    )
}

// ── Category filter row ───────────────────────────────────────────────────────

@Composable
private fun CatalogFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            CatalogFilterChip(
                label    = category,
                selected = category == selectedCategory,
                onClick  = { onCategorySelected(category) }
            )
        }
    }
}

/**
 * Reusable filter chip: dark blue when selected, light when not.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor         = Color.White,
            labelColor             = TextSecondary,
            selectedContainerColor = BrandBlue,
            selectedLabelColor     = Color.White,
            selectedLeadingIconColor = Color.White,
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor         = Color(0xFFE5E7EB),
            selectedBorderColor = Color.Transparent,
            selectedBorderWidth = 0.dp,
        )
    )
}

// ── Product count label ───────────────────────────────────────────────────────

@Composable
private fun ProductCountLabel(count: Int) {
    Text(
        text = if (count == 1) "1 producto" else "$count productos",
        style = MaterialTheme.typography.labelMedium,
        color = TextSecondary
    )
}

// ── Product card ──────────────────────────────────────────────────────────────

/**
 * Reusable product card: icon area with category colour, name,
 * description, category badge, price and — when known — stock.
 *
 * Stock is hidden when [stock] == [CatalogProduct.STOCK_UNKNOWN] (-1).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    name: String,
    description: String,
    category: String,
    price: Double,
    stock: Int,
    modifier: Modifier = Modifier
) {
    val iconBg   = categoryIconBg(category)
    val iconTint = categoryIconTint(category)
    val icon     = categoryIcon(category)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // ── Icon area ─────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = iconTint
                )

                // Category badge — top-right corner
                ProductCategoryBadge(
                    category = category,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }

            // ── Content ───────────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "€ %.2f".format(price),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue
                    )
                    // Only show stock when the backend provides it
                    if (stock >= 0) {
                        Text(
                            text = "Stock: $stock",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Small coloured pill that shows the product category.
 */
@Composable
private fun ProductCategoryBadge(category: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(categoryBadgeBg(category))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = categoryBadgeText(category)
        )
    }
}
