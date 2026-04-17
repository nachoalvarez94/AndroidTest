package com.example.distridulce.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.distridulce.model.OrderSession
import com.example.distridulce.repository.ClienteRepository
import com.example.distridulce.repository.FacturaRepository
import com.example.distridulce.repository.PedidoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// ── UI models ─────────────────────────────────────────────────────────────────

/**
 * A single row in the orders list, combining data from the pedido, its optional
 * factura, and the resolved client name.
 */
data class PedidoUiModel(
    val pedidoId: Long,
    val numero: Long,
    val clienteNombre: String,
    val fechaDisplay: String,       // formatted for display: "15/01/2024 10:30"
    val fechaTimestamp: Long,       // millis — used for the 24 h filter
    val total: Double,
    val estado: String,             // raw backend value: "PENDIENTE", "CONFIRMADO" …
    val factura: FacturaUiSummary?, // null = not yet invoiced
    /**
     * Three-valued payment state:
     *   true  = confirmed fully paid (recorded in [OrderSession.pagoCompleto])
     *   false = confirmed NOT fully paid (partial or pending payment)
     *   null  = unknown — order was not created in this session and the backend
     *           does not yet expose a payment-status field.
     *
     * Invoicing is only allowed when this is explicitly `true`.
     * Both `false` and `null` block the "Facturar" button with different messages.
     *
     * TODO: replace with a real backend field when the API exposes it.
     */
    val cobrado: Boolean?
)

/**
 * Minimal invoice info needed to drive the "Ver factura" / "Compartir" buttons.
 * Full details are fetched on-demand by [InvoiceViewModel] when the user
 * navigates to [InvoiceScreen].
 */
data class FacturaUiSummary(
    val facturaId: Long,
    val numeroFactura: Long,
    val fechaEmision: String        // raw string from the backend
)

// ── Billing action state ──────────────────────────────────────────────────────

/**
 * Tracks the state of an in-progress "Facturar" action.
 * Orthogonal to [OrdersUiState] so a billing error doesn't wipe the list.
 */
sealed class BillingAction {
    object Idle : BillingAction()
    data class InProgress(val pedidoId: Long) : BillingAction()
    data class Error(val pedidoId: Long, val message: String) : BillingAction()
}

// ── Screen state ──────────────────────────────────────────────────────────────

sealed class OrdersUiState {
    object Loading : OrdersUiState()
    data class Success(
        val pedidos: List<PedidoUiModel>,
        val filterLast24h: Boolean
    ) : OrdersUiState()
    data class Error(val message: String) : OrdersUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

/**
 * Drives [OrdersScreen].
 *
 * On load it fetches pedidos, facturas and clientes in parallel, then:
 *  - builds a `pedidoId → FacturaUiSummary` map from the facturas list
 *  - builds a `clienteId → name` map from the clientes list
 *  - assembles [PedidoUiModel] rows sorted newest-first
 *
 * The 24 h filter is applied client-side so toggling it is instant.
 *
 * The "Facturar" action calls POST /api/facturas/desde-pedido/{pedidoId} and
 * updates the affected row in-place without reloading the whole list.
 */
class OrdersViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    private val _billingAction = MutableStateFlow<BillingAction>(BillingAction.Idle)
    val billingAction: StateFlow<BillingAction> = _billingAction.asStateFlow()

    /** Full unfiltered list — kept in memory so toggling the filter is O(n). */
    private var allPedidos: List<PedidoUiModel> = emptyList()
    private var filterLast24h: Boolean = true

    init {
        loadData()
    }

    // ── Public actions ────────────────────────────────────────────────────────

    /** (Re-)loads all data from the backend. Resets to Loading first. */
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = OrdersUiState.Loading
            try {
                // All three requests run in parallel.
                val pedidosDeferred  = async { PedidoRepository.getPedidos() }
                val facturasDeferred = async { FacturaRepository.getFacturas() }
                val clientesDeferred = async { ClienteRepository.getClientes() }

                val pedidos  = pedidosDeferred.await()
                val facturas = facturasDeferred.await()
                val clientes = clientesDeferred.await()

                // Build lookup maps
                val facturaByPedidoId: Map<Long, FacturaUiSummary> = facturas
                    .associateBy(
                        keySelector   = { it.pedidoId },
                        valueTransform = { FacturaUiSummary(it.id, it.numeroFactura, it.fechaEmision) }
                    )
                val nombreByClienteId: Map<Long, String> = clientes
                    .mapNotNull { client ->
                        client.id.toLongOrNull()?.let { id -> id to client.name }
                    }
                    .toMap()

                allPedidos = pedidos
                    .map { pedido ->
                        PedidoUiModel(
                            pedidoId      = pedido.id,
                            numero        = pedido.numero,
                            clienteNombre = nombreByClienteId[pedido.clienteId]
                                ?: "#${pedido.clienteId}",
                            fechaDisplay  = formatFecha(pedido.fecha),
                            fechaTimestamp = parseToMillis(pedido.fecha),
                            total         = pedido.totalFinal,
                            estado        = pedido.estado,
                            factura  = facturaByPedidoId[pedido.id],
                            // null when the order was not created in this session —
                            // the backend does not yet send a payment-status field.
                            // Never default to true: unknown ≠ paid.
                            cobrado  = OrderSession.pagoCompleto[pedido.id]
                        )
                    }
                    .sortedByDescending { it.fechaTimestamp }

                _uiState.value = OrdersUiState.Success(
                    pedidos       = applyFilter(),
                    filterLast24h = filterLast24h
                )
            } catch (e: Exception) {
                _uiState.value = OrdersUiState.Error(friendlyError(e))
            }
        }
    }

    /** Toggles between "últimas 24 h" and "todos" and refreshes the displayed list. */
    fun toggleFilter() {
        filterLast24h = !filterLast24h
        val current = _uiState.value
        if (current is OrdersUiState.Success) {
            _uiState.value = current.copy(
                pedidos       = applyFilter(),
                filterLast24h = filterLast24h
            )
        }
    }

    /**
     * Creates an invoice for [pedidoId] via POST /api/facturas/desde-pedido/{pedidoId}.
     * Updates the affected row in-place on success; sets [BillingAction.Error] on failure.
     */
    fun facturarPedido(pedidoId: Long) {
        if (_billingAction.value is BillingAction.InProgress) return  // guard double-tap

        // Business rule: only fully-paid orders can be invoiced.
        // cobrado == true  → paid, allow
        // cobrado == false → known unpaid, block
        // cobrado == null  → unknown (order from a previous session), block
        val pedido = allPedidos.find { it.pedidoId == pedidoId }
        if (pedido != null && pedido.cobrado != true) {
            val msg = if (pedido.cobrado == false)
                "El pedido tiene pago parcial o pendiente. " +
                "Completa el cobro antes de generar la factura."
            else
                "No hay información de cobro disponible para este pedido. " +
                "Confirma el estado de pago antes de facturar."
            _billingAction.value = BillingAction.Error(pedidoId = pedidoId, message = msg)
            return
        }

        viewModelScope.launch {
            _billingAction.value = BillingAction.InProgress(pedidoId)
            try {
                val factura = FacturaRepository.createFacturaFromPedido(pedidoId)
                val summary = FacturaUiSummary(factura.id, factura.numeroFactura, factura.fechaEmision)

                // Update the row in-place — no full reload needed.
                allPedidos = allPedidos.map { pedido ->
                    if (pedido.pedidoId == pedidoId) pedido.copy(factura = summary) else pedido
                }

                val current = _uiState.value
                if (current is OrdersUiState.Success) {
                    _uiState.value = current.copy(pedidos = applyFilter())
                }
                _billingAction.value = BillingAction.Idle
            } catch (e: Exception) {
                _billingAction.value = BillingAction.Error(pedidoId, friendlyError(e))
            }
        }
    }

    /** Clears a [BillingAction.Error] so the card returns to its default state. */
    fun dismissBillingError() {
        _billingAction.value = BillingAction.Idle
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun applyFilter(): List<PedidoUiModel> {
        if (!filterLast24h) return allPedidos
        val cutoff = System.currentTimeMillis() - 24L * 60 * 60 * 1000
        return allPedidos.filter { it.fechaTimestamp >= cutoff }
    }

    private fun friendlyError(e: Exception): String = when {
        e.message?.contains("Unable to resolve host") == true ->
            "No se puede conectar al servidor.\nVerifica la URL en RetrofitClient."
        e.message?.contains("timeout") == true ->
            "La petición tardó demasiado. Inténtalo de nuevo."
        else -> e.message ?: "Error desconocido."
    }

    // ── Date helpers ──────────────────────────────────────────────────────────

    private val inputFormats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd"
    )

    private fun parseToMillis(dateStr: String): Long {
        for (fmt in inputFormats) {
            try {
                val parsed = SimpleDateFormat(fmt, Locale.getDefault()).parse(dateStr)
                if (parsed != null) return parsed.time
            } catch (_: Exception) { /* try next */ }
        }
        return 0L
    }

    private fun formatFecha(dateStr: String): String {
        val locale = Locale("es", "ES")
        val outputLong  = SimpleDateFormat("dd/MM/yyyy HH:mm", locale)
        val outputShort = SimpleDateFormat("dd/MM/yyyy", locale)
        for (fmt in inputFormats) {
            try {
                val parsed = SimpleDateFormat(fmt, Locale.getDefault()).parse(dateStr)
                if (parsed != null) {
                    return if (fmt == "yyyy-MM-dd") outputShort.format(parsed)
                    else outputLong.format(parsed)
                }
            } catch (_: Exception) { /* try next */ }
        }
        return dateStr  // fallback
    }
}
