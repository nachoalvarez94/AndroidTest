@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.distridulce.ui.orders

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.distridulce.model.CatalogProduct
import com.example.distridulce.ui.catalog.CatalogUiState
import com.example.distridulce.ui.catalog.CatalogViewModel
import com.example.distridulce.ui.theme.BackgroundLight
import com.example.distridulce.ui.theme.BrandBlue
import com.example.distridulce.ui.theme.TextPrimary
import com.example.distridulce.ui.theme.TextSecondary

// ── Palette (mirrors OrdersScreen) ───────────────────────────────────────────

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

/**
 * Full-screen split panel for editing an existing order:
 *  - Left (60 %) — product catalogue (same as OrderBuilderScreen) to add items.
 *  - Right (40 %) — editable order lines + cobro section + save/cancel actions.
 *
 * The layout intentionally mirrors [OrderBuilderScreen] so the user experience
 * is consistent.  [CatalogViewModel] is injected independently of
 * [EditOrderViewModel] so the two loading states are orthogonal.
 */
@Composable
fun EditOrderScreen(
    viewModel: EditOrderViewModel,
    onSaved: () -> Unit = {},
    onBack: () -> Unit = {},
    catalogViewModel: CatalogViewModel = viewModel()
) {
    val loadState  by viewModel.loadState.collectAsState()
    val saveState  by viewModel.saveState.collectAsState()
    val catalogState by catalogViewModel.uiState.collectAsState()

    // Map backend products to [CatalogProduct] list for the left panel.
    val catalogProducts = remember(catalogState) {
        (catalogState as? CatalogUiState.Success)?.products ?: emptyList()
    }

    // Navigate away as soon as the save succeeds.
    LaunchedEffect(saveState) {
        if (saveState is EditSaveState.Saved) onSaved()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = BackgroundLight
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            // ── Left panel — product catalogue (60 %) ─────────────────────────
            EditCatalogSection(
                modifier     = Modifier.weight(0.6f),
                products     = catalogProducts,
                isLoading    = catalogState is CatalogUiState.Loading,
                errorMessage = (catalogState as? CatalogUiState.Error)?.message,
                onRetry      = catalogViewModel::loadProducts,
                onAddProduct = viewModel::addProduct
            )

            // Vertical divider
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )

            // ── Right panel — editable order (40 %) ───────────────────────────
            EditOrderPanel(
                modifier      = Modifier.weight(0.4f),
                loadState     = loadState,
                saveState     = saveState,
                editLines     = viewModel.editLines,
                importeCobrado = viewModel.importeCobrado.collectAsState().value,
                onIncrement   = viewModel::incrementLine,
                onDecrement   = viewModel::decrementLine,
                onRemove      = viewModel::removeLine,
                onImporteChange = viewModel::updateImporteCobrado,
                onSave        = viewModel::save,
                onDismissError = viewModel::dismissSaveError,
                onBack        = onBack,
                onRetry       = viewModel::loadPedido
            )
        }
    }
}

// ── Left panel — product catalogue ───────────────────────────────────────────

/**
 * Catalogue section for the edit screen.  Functionally identical to
 * [OrderBuilderScreen]'s private `ProductCatalogSection` but without the
 * cancel (×) button — the user exits via the back button in the right panel.
 */
@Composable
private fun EditCatalogSection(
    modifier: Modifier = Modifier,
    products: List<CatalogProduct>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onAddProduct: (CatalogProduct) -> Unit
) {
    val categories = remember(products) {
        listOf("Todos") + products.map { it.category }.distinct().sorted()
    }

    var query    by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("Todos") }

    LaunchedEffect(categories) {
        if (selected !in categories) selected = "Todos"
    }

    val filtered = remember(query, selected, products) {
        products.filter { p ->
            val matchCategory = selected == "Todos" || p.category == selected
            val matchQuery    = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.category.contains(query, ignoreCase = true)
            matchCategory && matchQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Text(
            text       = "Añadir productos",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = TextPrimary
        )

        // Search bar
        OutlinedTextField(
            value         = query,
            onValueChange = { query = it },
            modifier      = Modifier.fillMaxWidth(),
            placeholder   = { Text("Buscar producto…", color = TextSecondary) },
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

        // Category chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { cat ->
                FilterChip(
                    selected = selected == cat,
                    onClick  = { selected = cat },
                    label    = { Text(cat) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandBlue,
                        selectedLabelColor     = Color.White
                    )
                )
            }
        }

        // Content
        when {
            isLoading -> {
                Box(
                    modifier          = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment  = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(color = BrandBlue)
                        Text("Cargando productos…", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }

            errorMessage != null -> {
                Box(
                    modifier         = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier            = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint               = Color(0xFFDC2626),
                            modifier           = Modifier.size(40.dp)
                        )
                        Text(
                            text      = errorMessage,
                            style     = MaterialTheme.typography.bodySmall,
                            color     = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        OutlinedButton(onClick = onRetry) {
                            Text("Reintentar", color = BrandBlue)
                        }
                    }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns             = GridCells.Adaptive(minSize = 180.dp),
                    contentPadding      = PaddingValues(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement   = Arrangement.spacedBy(12.dp),
                    modifier            = Modifier.weight(1f)
                ) {
                    items(items = filtered, key = { it.id }) { product ->
                        EditCatalogProductCard(product = product, onAdd = { onAddProduct(product) })
                    }
                }
            }
        }
    }
}

// ── Catalog product card (left panel) ────────────────────────────────────────

private fun editCardIconBg(category: String): Color = when (category) {
    "Galletas" -> Color(0xFFFEF3C7)
    "Bollería" -> Color(0xFFFEE2E2)
    "Dulces"   -> Color(0xFFEDE9FE)
    else       -> Color(0xFFDCEBFD)
}

private fun editCardIconTint(category: String): Color = when (category) {
    "Galletas" -> Color(0xFFD97706)
    "Bollería" -> Color(0xFFDC2626)
    "Dulces"   -> Color(0xFF7C3AED)
    else       -> Color(0xFF2563EB)
}

private fun editCardIcon(category: String): ImageVector = when (category) {
    "Galletas" -> Icons.Filled.LocalGroceryStore
    "Bollería" -> Icons.Filled.Fastfood
    "Dulces"   -> Icons.Filled.Cake
    else       -> Icons.Filled.Inventory2
}

@Composable
private fun EditCatalogProductCard(product: CatalogProduct, onAdd: () -> Unit) {
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
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(editCardIconBg(product.category)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = editCardIcon(product.category),
                    contentDescription = null,
                    tint               = editCardIconTint(product.category),
                    modifier           = Modifier.size(32.dp)
                )
            }

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

            Text(
                text       = "€ %.2f".format(product.price),
                style      = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color      = BrandBlue
            )

            Button(
                onClick        = onAdd,
                modifier       = Modifier.fillMaxWidth(),
                shape          = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                colors         = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Añadir", style = MaterialTheme.typography.labelMedium, color = Color.White)
            }
        }
    }
}

// ── Right panel — editable order ─────────────────────────────────────────────

@Composable
private fun EditOrderPanel(
    modifier: Modifier = Modifier,
    loadState: EditLoadState,
    saveState: EditSaveState,
    editLines: List<EditLine>,
    importeCobrado: String,
    onIncrement: (Long) -> Unit,
    onDecrement: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onImporteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    val isSaving = saveState is EditSaveState.Saving

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(ColorCard)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── Panel header ──────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = ColorNeutral)
            }
            when (val s = loadState) {
                is EditLoadState.Loaded -> {
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                        Text(
                            text       = "PED-%05d".format(s.pedido.numero),
                            fontWeight = FontWeight.Bold,
                            fontSize   = 15.sp,
                            color      = ColorTextPrimary
                        )
                        Text(
                            text     = s.pedido.estado.lowercase()
                                .replaceFirstChar { it.uppercaseChar() },
                            fontSize = 12.sp,
                            color    = ColorTextSecondary
                        )
                    }
                }
                else -> Spacer(modifier = Modifier.weight(1f))
            }
        }

        Divider(color = ColorDivider, modifier = Modifier.padding(vertical = 8.dp))

        // ── Save error banner ─────────────────────────────────────────────────
        if (saveState is EditSaveState.Error) {
            Surface(
                color    = ColorErrorLight,
                shape    = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text     = saveState.message,
                        fontSize = 12.sp,
                        color    = ColorError,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismissError, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector        = Icons.Filled.ErrorOutline,
                            contentDescription = "Cerrar",
                            tint               = ColorError,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // ── Main content (scrollable) ─────────────────────────────────────────
        when (loadState) {
            is EditLoadState.Loading -> {
                Box(
                    modifier         = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(color = BrandBlue)
                        Text("Cargando pedido…", fontSize = 13.sp, color = ColorTextSecondary)
                    }
                }
            }

            is EditLoadState.Error -> {
                Box(
                    modifier         = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier            = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint     = ColorError,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text      = loadState.message,
                            fontSize  = 13.sp,
                            color     = ColorTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        OutlinedButton(onClick = onRetry) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            is EditLoadState.Loaded -> {
                LazyColumn(
                    modifier        = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // ── Section title ─────────────────────────────────────────
                    item {
                        Text(
                            text       = "Líneas del pedido",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = ColorTextSecondary,
                            modifier   = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // ── Edit lines ────────────────────────────────────────────
                    if (editLines.isEmpty()) {
                        item {
                            Box(
                                modifier         = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Sin productos. Añade desde el catálogo.",
                                    fontSize  = 13.sp,
                                    color     = ColorTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(items = editLines, key = { it.articuloId }) { line ->
                            EditLineRow(
                                line        = line,
                                onIncrement = { onIncrement(line.articuloId) },
                                onDecrement = { onDecrement(line.articuloId) },
                                onRemove    = { onRemove(line.articuloId) }
                            )
                            Divider(
                                color    = ColorDivider,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    // ── Cobro section ─────────────────────────────────────────
                    item {
                        Spacer(Modifier.height(8.dp))

                        // Live preview: recalculate client-side whenever lines or
                        // the collected amount change.  The backend remains the
                        // source of truth — these values are shown as estimates only.
                        val totalEstimado = editLines.sumOf { it.precioUnitario * it.cantidad }
                        val cobradoDouble = importeCobrado.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val pendienteEstimado = maxOf(totalEstimado - cobradoDouble, 0.0)

                        CobroSection(
                            estadoCobroActual = loadState.pedido.estadoCobro,
                            importeCobrado    = importeCobrado,
                            totalEstimado     = totalEstimado,
                            pendienteEstimado = pendienteEstimado,
                            onImporteChange   = onImporteChange,
                            onCompletar       = {
                                onImporteChange("%.2f".format(totalEstimado))
                            }
                        )
                    }
                }
            }
        }

        // ── Action buttons (always visible at bottom) ─────────────────────────
        Spacer(Modifier.height(12.dp))
        Divider(color = ColorDivider)
        Spacer(Modifier.height(12.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick  = onBack,
                modifier = Modifier.weight(1f),
                enabled  = !isSaving
            ) {
                Text("Cancelar")
            }

            Button(
                onClick  = onSave,
                modifier = Modifier.weight(2f),
                enabled  = loadState is EditLoadState.Loaded && !isSaving,
                colors   = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Guardando…")
                } else {
                    Icon(
                        Icons.Filled.Save,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Guardar cambios")
                }
            }
        }
    }
}

// ── Edit line row ─────────────────────────────────────────────────────────────

@Composable
private fun EditLineRow(
    line: EditLine,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    val cantidadLabel = if (line.cantidad == kotlin.math.floor(line.cantidad))
        line.cantidad.toLong().toString()
    else
        "%.1f".format(line.cantidad)

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product name + unit price
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = line.nombre,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = ColorTextPrimary,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis
            )
            Text(
                text     = "€ %.2f / ud".format(line.precioUnitario),
                fontSize = 11.sp,
                color    = ColorTextSecondary
            )
        }

        Spacer(Modifier.width(8.dp))

        // Qty controls: [-] [N] [+]
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(
                onClick  = onDecrement,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    Icons.Filled.Remove,
                    contentDescription = "Decrementar",
                    modifier           = Modifier.size(14.dp),
                    tint               = ColorPrimary
                )
            }

            Box(
                modifier         = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ColorPrimaryLight)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = cantidadLabel,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = ColorPrimary
                )
            }

            IconButton(
                onClick  = onIncrement,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Incrementar",
                    modifier           = Modifier.size(14.dp),
                    tint               = ColorPrimary
                )
            }
        }

        Spacer(Modifier.width(4.dp))

        // Delete button
        IconButton(
            onClick  = onRemove,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Eliminar línea",
                modifier           = Modifier.size(16.dp),
                tint               = ColorError
            )
        }
    }
}

// ── Cobro section ─────────────────────────────────────────────────────────────

/**
 * Shows payment information for the order being edited.
 *
 * [totalEstimado] and [pendienteEstimado] are derived locally from the current
 * edit lines and [importeCobrado] — they are preview-only values.  The backend
 * recalculates the real totals when the order is saved.
 *
 * [estadoCobroActual] is the last persisted value from the backend response;
 * it is shown as a reference and labelled "Estado actual" to distinguish it
 * from the live estimates.
 */
@Composable
private fun CobroSection(
    estadoCobroActual: String?,
    importeCobrado: String,
    totalEstimado: Double,
    pendienteEstimado: Double,
    onImporteChange: (String) -> Unit,
    onCompletar: () -> Unit
) {
    val pagarTodoEnabled = totalEstimado > 0.0

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = ColorNeutralLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text       = "Cobro",
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = ColorTextSecondary
            )

            // ── Total estimado (live) ──────────────────────────────────────────
            CobroReadOnlyRow(
                label      = "Total estimado",
                value      = "€ %.2f".format(totalEstimado),
                valueColor = ColorPrimary
            )

            // ── Importe cobrado + "Pagar todo" button ─────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value           = importeCobrado,
                    onValueChange   = onImporteChange,
                    label           = { Text("Cobrado (€)", fontSize = 12.sp) },
                    modifier        = Modifier.weight(1f),
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape           = RoundedCornerShape(8.dp),
                    colors          = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor       = Color.White,
                        unfocusedBorderColor = ColorDivider,
                        focusedBorderColor   = BrandBlue
                    )
                )

                // Small "Pagar todo" button — fills importeCobrado with totalEstimado.
                OutlinedButton(
                    onClick  = onCompletar,
                    enabled  = pagarTodoEnabled,
                    modifier = Modifier.height(48.dp),
                    shape    = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Text(
                        text     = "Pagar todo",
                        fontSize = 12.sp,
                        color    = if (pagarTodoEnabled) BrandBlue else ColorNeutral
                    )
                }
            }

            // ── Pendiente estimado (live) ──────────────────────────────────────
            CobroReadOnlyRow(
                label      = "Pendiente estimado",
                value      = "€ %.2f".format(pendienteEstimado),
                valueColor = when {
                    pendienteEstimado <= 0.0 -> ColorSuccess
                    else                     -> ColorWarning
                }
            )

            Divider(color = ColorDivider)

            // ── Estado actual del backend (referencia estática) ────────────────
            CobroReadOnlyRow(
                label      = "Estado actual",
                value      = when (estadoCobroActual) {
                    "COMPLETO"  -> "Completo"
                    "PARCIAL"   -> "Parcial"
                    "PENDIENTE" -> "Pendiente"
                    else        -> estadoCobroActual ?: "—"
                },
                valueColor = when (estadoCobroActual) {
                    "COMPLETO"  -> ColorSuccess
                    "PARCIAL",
                    "PENDIENTE" -> ColorWarning
                    else        -> ColorTextSecondary
                }
            )

            // Disclaimer
            Text(
                text     = "Los totales son orientativos. El estado de cobro se recalculará al guardar.",
                fontSize = 10.sp,
                color    = ColorTextSecondary
            )
        }
    }
}

@Composable
private fun CobroReadOnlyRow(
    label: String,
    value: String,
    valueColor: Color = ColorTextPrimary
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = ColorTextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}
