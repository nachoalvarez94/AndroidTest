package com.example.distridulce.ui.orders

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.distridulce.model.OrderSession
import com.example.distridulce.network.dto.FacturaResponseDto
import com.example.distridulce.repository.FacturaRepository
import com.example.distridulce.repository.FacturaPdfRepository
import com.example.distridulce.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

// ── PDF action state ──────────────────────────────────────────────────────────

/**
 * Tracks the one-at-a-time PDF download triggered by "Compartir" or "Imprimir".
 * Kept separate from [InvoiceUiState] so a PDF error never wipes the invoice data.
 */
sealed class PdfActionState {
    /** No PDF action in progress. */
    object Idle : PdfActionState()

    /** Downloading the PDF from the backend (or reading from cache). */
    object Downloading : PdfActionState()

    /** Download failed — [message] is shown to the user. */
    data class Error(val message: String) : PdfActionState()
}

// ── Invoice UI state ──────────────────────────────────────────────────────────

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

    private val _pdfActionState = MutableStateFlow<PdfActionState>(PdfActionState.Idle)
    val pdfActionState: StateFlow<PdfActionState> = _pdfActionState.asStateFlow()

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
     * Ensures the PDF for the current invoice is available locally, then calls
     * [onFile] with the resulting [File].
     *
     * Serves the file from cache when possible; downloads from the backend otherwise.
     * [context] is only used inside the coroutine (never stored) so it is safe to
     * pass an Activity or application context.
     *
     * @param onFile  Called on the main thread once the file is ready.
     *                Typical usage: call [PdfShareHelper.share] or [PdfPrintHelper.print].
     */
    fun downloadAndAction(context: Context, onFile: (File) -> Unit) {
        if (_pdfActionState.value is PdfActionState.Downloading) return

        val facturaId = (_uiState.value as? InvoiceUiState.Success)?.factura?.id
            ?: return  // invoice not loaded yet — button should be disabled in this state

        viewModelScope.launch {
            _pdfActionState.value = PdfActionState.Downloading
            try {
                val file = FacturaPdfRepository.getCachedPdf(context, facturaId)
                    ?: FacturaPdfRepository.downloadPdf(context, facturaId)

                _pdfActionState.value = PdfActionState.Idle
                onFile(file)
            } catch (e: Exception) {
                _pdfActionState.value = PdfActionState.Error(
                    e.message ?: "Error al obtener el PDF de la factura."
                )
            }
        }
    }

    /** Clears a [PdfActionState.Error] so the button returns to its normal state. */
    fun dismissPdfError() {
        if (_pdfActionState.value is PdfActionState.Error) {
            _pdfActionState.value = PdfActionState.Idle
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
