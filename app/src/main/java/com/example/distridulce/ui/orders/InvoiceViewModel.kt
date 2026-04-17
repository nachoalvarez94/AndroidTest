package com.example.distridulce.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.distridulce.model.OrderSession
import com.example.distridulce.network.dto.FacturaResponseDto
import com.example.distridulce.repository.FacturaRepository
import com.example.distridulce.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI state ──────────────────────────────────────────────────────────────────

sealed class InvoiceUiState {
    /** Invoice is being generated / fetched. */
    object Loading : InvoiceUiState()

    /**
     * Invoice created and ready to display.
     *
     * Payment fields ([estadoCobro], [importeCobrado], [importePendiente]) come
     * from the associated Pedido — the Factura DTO does not carry them.
     * All three are nullable to handle legacy orders that predate the fields.
     */
    data class Success(
        val factura: FacturaResponseDto,
        val estadoCobro: String?,
        val importeCobrado: Double?,
        val importePendiente: Double?
    ) : InvoiceUiState()

    /** Network or server error. */
    data class Error(val message: String) : InvoiceUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

/**
 * Drives [InvoiceScreen].
 *
 * On first load it calls POST /api/facturas/desde-pedido/{pedidoId}.
 * If [OrderSession.facturaId] is already set (e.g. screen is revisited within
 * the same session), it falls back to GET /api/facturas/{id} to avoid
 * creating a duplicate invoice.
 */
class InvoiceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<InvoiceUiState>(InvoiceUiState.Loading)
    val uiState: StateFlow<InvoiceUiState> = _uiState.asStateFlow()

    init {
        generateFactura()
    }

    /** Triggers invoice generation (or re-fetch on retry). */
    fun generateFactura() {
        viewModelScope.launch {
            _uiState.value = InvoiceUiState.Loading
            try {
                val factura = resolveFactura()
                // Cache the IDs so a subsequent call uses GET instead of POST.
                OrderSession.facturaId     = factura.id
                OrderSession.facturaNumero = factura.numeroFactura

                // Fetch the associated pedido to get the real payment status.
                // FacturaResponseDto does not carry estadoCobro / importeCobrado /
                // importePendiente — those fields live on the Pedido.
                val pedido = PedidoRepository.getPedidoById(factura.pedidoId)

                _uiState.value = InvoiceUiState.Success(
                    factura          = factura,
                    estadoCobro      = pedido.estadoCobro,
                    importeCobrado   = pedido.importeCobrado,
                    importePendiente = pedido.importePendiente
                )
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("Unable to resolve host") == true ->
                        "No se puede conectar al servidor.\nVerifica la URL en RetrofitClient."
                    e.message?.contains("timeout") == true ->
                        "La petición tardó demasiado. Inténtalo de nuevo."
                    else ->
                        e.message ?: "Error desconocido al generar la factura."
                }
                _uiState.value = InvoiceUiState.Error(message)
            }
        }
    }

    /**
     * If this invoice was already created in the current session, fetches it
     * by ID (GET).  Otherwise creates it from the active pedido (POST).
     */
    private suspend fun resolveFactura(): FacturaResponseDto {
        val existingFacturaId = OrderSession.facturaId
        if (existingFacturaId != null) {
            return FacturaRepository.getFactura(existingFacturaId)
        }
        val pedidoId = OrderSession.pedidoId
            ?: error("No hay pedido activo en la sesión. Vuelve a crear el pedido.")
        return FacturaRepository.createFacturaFromPedido(pedidoId)
    }
}
