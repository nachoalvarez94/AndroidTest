package com.example.distridulce.ui.orders

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.distridulce.model.CatalogProduct
import com.example.distridulce.network.dto.PedidoLineaRequestDto
import com.example.distridulce.network.dto.PedidoRequestDto
import com.example.distridulce.network.dto.PedidoResponseDto
import com.example.distridulce.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── Domain model ──────────────────────────────────────────────────────────────

/**
 * One editable line in an order being modified.
 *
 * Kept separate from [CartItem] because the edit flow works directly with
 * backend IDs and does not need the option/key abstraction used in order creation.
 */
data class EditLine(
    val articuloId: Long,
    val nombre: String,
    val precioUnitario: Double,
    val cantidad: Double
)

// ── Screen states ─────────────────────────────────────────────────────────────

sealed class EditLoadState {
    object Loading : EditLoadState()
    data class Loaded(val pedido: PedidoResponseDto) : EditLoadState()
    data class Error(val message: String) : EditLoadState()
}

sealed class EditSaveState {
    object Idle    : EditSaveState()
    object Saving  : EditSaveState()
    object Saved   : EditSaveState()
    data class Error(val message: String) : EditSaveState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

/**
 * Drives [EditOrderScreen].
 *
 * On init it fetches the pedido by [pedidoId] and populates [editLines] and
 * [importeCobrado] from the response.  The user can then add/remove/reorder
 * lines and adjust the collected amount.
 *
 * [save] builds a [PedidoRequestDto] with `estadoCobro = null` so the backend
 * recalculates it from [importeCobrado] and the new total — the app never
 * derives estadoCobro client-side.
 *
 * Instantiate via [Factory] to pass [pedidoId] into the ViewModel.
 */
class EditOrderViewModel(private val pedidoId: Long) : ViewModel() {

    // ── State ─────────────────────────────────────────────────────────────────

    private val _loadState = MutableStateFlow<EditLoadState>(EditLoadState.Loading)
    val loadState: StateFlow<EditLoadState> = _loadState.asStateFlow()

    private val _saveState = MutableStateFlow<EditSaveState>(EditSaveState.Idle)
    val saveState: StateFlow<EditSaveState> = _saveState.asStateFlow()

    /** Mutable snapshot list — mutations trigger Compose recomposition automatically. */
    val editLines = mutableStateListOf<EditLine>()

    /**
     * String-backed so it binds directly to a TextField without conversion.
     * Parsed to Double (replacing comma with period) when building the request.
     */
    private val _importeCobrado = MutableStateFlow("")
    val importeCobrado: StateFlow<String> = _importeCobrado.asStateFlow()

    // Retained from the backend response — needed when building the PUT payload.
    private var clienteId: Long = 0L
    private var observaciones: String? = null

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        loadPedido()
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    fun loadPedido() {
        viewModelScope.launch {
            _loadState.value = EditLoadState.Loading
            try {
                val pedido = PedidoRepository.getPedidoById(pedidoId)

                clienteId     = pedido.clienteId
                observaciones = pedido.observaciones

                editLines.clear()
                editLines.addAll(
                    pedido.lineas.mapNotNull { linea ->
                        val artId = linea.articuloId ?: return@mapNotNull null
                        EditLine(
                            articuloId     = artId,
                            nombre         = linea.nombreArticulo,
                            precioUnitario = linea.precioUnitario,
                            cantidad       = linea.cantidad
                        )
                    }
                )

                // Pre-populate the payment field with the backend value (if any).
                _importeCobrado.value = pedido.importeCobrado
                    ?.let { "%.2f".format(it) }
                    ?: ""

                _loadState.value = EditLoadState.Loaded(pedido)
            } catch (e: Exception) {
                _loadState.value = EditLoadState.Error(friendlyError(e))
            }
        }
    }

    // ── Line editing ──────────────────────────────────────────────────────────

    fun incrementLine(articuloId: Long) {
        val idx = editLines.indexOfFirst { it.articuloId == articuloId }
        if (idx >= 0) {
            editLines[idx] = editLines[idx].copy(cantidad = editLines[idx].cantidad + 1.0)
        }
    }

    fun decrementLine(articuloId: Long) {
        val idx = editLines.indexOfFirst { it.articuloId == articuloId }
        if (idx < 0) return
        val line = editLines[idx]
        if (line.cantidad > 1.0) {
            editLines[idx] = line.copy(cantidad = line.cantidad - 1.0)
        } else {
            editLines.removeAt(idx)
        }
    }

    fun removeLine(articuloId: Long) {
        editLines.removeAll { it.articuloId == articuloId }
    }

    /**
     * Adds [product] to the edit lines, incrementing the quantity if it is
     * already present.  Products whose [CatalogProduct.id] cannot be parsed
     * as a Long are silently ignored (should not happen with real backend data).
     */
    fun addProduct(product: CatalogProduct) {
        val articuloId = product.id.toLongOrNull() ?: return
        val idx = editLines.indexOfFirst { it.articuloId == articuloId }
        if (idx >= 0) {
            editLines[idx] = editLines[idx].copy(cantidad = editLines[idx].cantidad + 1.0)
        } else {
            editLines.add(
                EditLine(
                    articuloId     = articuloId,
                    nombre         = product.name,
                    precioUnitario = product.price,
                    cantidad       = 1.0
                )
            )
        }
    }

    fun updateImporteCobrado(value: String) {
        _importeCobrado.value = value
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    /**
     * Sends PUT /api/pedidos/{pedidoId} with the current edited state.
     *
     * `estadoCobro` is intentionally sent as `null` so the backend recalculates
     * it from the new `importeCobrado` and the order total — the app never derives
     * estadoCobro client-side.
     */
    fun save() {
        if (_saveState.value is EditSaveState.Saving) return  // guard double-tap

        viewModelScope.launch {
            _saveState.value = EditSaveState.Saving
            try {
                val request = PedidoRequestDto(
                    clienteId      = clienteId,
                    observaciones  = observaciones,
                    lineas         = editLines.map {
                        PedidoLineaRequestDto(
                            articuloId = it.articuloId,
                            cantidad   = it.cantidad
                        )
                    },
                    // Replace comma with period for locales that use comma as decimal separator.
                    importeCobrado = _importeCobrado.value
                        .replace(",", ".")
                        .toDoubleOrNull(),
                    estadoCobro    = null   // backend recalculates
                )
                PedidoRepository.updatePedido(pedidoId, request)
                _saveState.value = EditSaveState.Saved
            } catch (e: Exception) {
                _saveState.value = EditSaveState.Error(friendlyError(e))
            }
        }
    }

    fun dismissSaveError() {
        if (_saveState.value is EditSaveState.Error) {
            _saveState.value = EditSaveState.Idle
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun friendlyError(e: Exception): String = when {
        e.message?.contains("Unable to resolve host") == true ->
            "No se puede conectar al servidor.\nVerifica la URL en RetrofitClient."
        e.message?.contains("timeout") == true ->
            "La petición tardó demasiado. Inténtalo de nuevo."
        else -> e.message ?: "Error desconocido."
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(private val pedidoId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EditOrderViewModel(pedidoId) as T
    }
}
