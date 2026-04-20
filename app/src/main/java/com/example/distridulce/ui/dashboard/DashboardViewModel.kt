package com.example.distridulce.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.distridulce.repository.ArticuloRepository
import com.example.distridulce.repository.ClienteRepository
import com.example.distridulce.repository.FacturaRepository
import com.example.distridulce.repository.PedidoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Data ──────────────────────────────────────────────────────────────────────

/**
 * Aggregated KPI figures shown in the metric cards.
 *
 * All values are derived from real backend data.  When a future user-scoped API
 * is available, add a `userId` filter in [DashboardViewModel.loadDashboard] —
 * no changes to this class or the UI will be necessary.
 */
data class DashboardMetrics(
    /** Orders whose [fecha] starts with today's yyyy-MM-dd prefix. */
    val pedidosHoy: Int,
    /** Orders that do not yet have an associated factura. */
    val pendientesFacturar: Int,
    /** Active articles returned by the catalogue endpoint. */
    val productosActivos: Int,
    /** Sum of all factura totals emitted in the current calendar month. */
    val ventasMes: Double
)

/**
 * Lightweight summary of a single pedido shown in the "Últimos Pedidos" section.
 *
 * The client name is resolved at load time by joining against [ClienteRepository];
 * it falls back to "Cliente #id" if the client is not found.
 */
data class PedidoSummary(
    val id: Long,
    val numero: Long,
    val clienteName: String,
    /** Raw ISO-8601 date string from the backend (yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss). */
    val fecha: String,
    val totalFinal: Double,
    /** "COMPLETO", "PARCIAL", "PENDIENTE", or null for legacy records. */
    val estadoCobro: String?
)

// ── UI state ──────────────────────────────────────────────────────────────────

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val metrics: DashboardMetrics,
        val recentPedidos: List<PedidoSummary>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

/**
 * Loads dashboard data from four backend endpoints in parallel.
 *
 * Future readiness: when per-user filtering is available, add a userId param to
 * [loadDashboard] and propagate it to the relevant repository calls.  The UI
 * layer ([DashboardScreen]) will need no changes.
 */
class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { loadDashboard() }

    /** Re-fetches all dashboard data; safe to call on pull-to-refresh or retry. */
    fun refresh() = loadDashboard()

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                // All four network calls run concurrently.
                val dPedidos   = async { PedidoRepository.getPedidos() }
                val dFacturas  = async { FacturaRepository.getFacturas() }
                val dArticulos = async { ArticuloRepository.getArticulos() }
                val dClientes  = async { ClienteRepository.getClientes() }

                val pedidos   = dPedidos.await()
                val facturas  = dFacturas.await()
                val articulos = dArticulos.await()
                val clientes  = dClientes.await()

                val locale = Locale.getDefault()

                // ClienteDto.id (Long) is mapped to Client.id as id.toString()
                // in ClienteMapper.kt, so the key comparison is String-based.
                val clienteMap = clientes.associateBy { it.id }

                val todayPrefix = SimpleDateFormat("yyyy-MM-dd", locale).format(Date())
                val monthPrefix = SimpleDateFormat("yyyy-MM",    locale).format(Date())

                // ── Metrics ───────────────────────────────────────────────────

                val pedidosHoy = pedidos.count { it.fecha.startsWith(todayPrefix) }

                // Pedidos whose id appears in at least one FacturaResponseDto.pedidoId
                // are already invoiced; the rest are "pendientes de facturar".
                val facturadoIds       = facturas.map { it.pedidoId }.toSet()
                val pendientesFacturar = pedidos.count { it.id !in facturadoIds }

                // ArticuloRepository already filters to active articles.
                val productosActivos = articulos.size

                // Sum factura totals emitted this month.
                val ventasMes = facturas
                    .filter { it.fechaEmision.startsWith(monthPrefix) }
                    .sumOf { it.total }

                // ── Recent pedidos (last 5, newest first) ─────────────────────

                val recentPedidos = pedidos
                    .sortedByDescending { it.fecha }
                    .take(5)
                    .map { pedido ->
                        PedidoSummary(
                            id          = pedido.id,
                            numero      = pedido.numero,
                            clienteName = clienteMap[pedido.clienteId.toString()]?.name
                                ?: "Cliente #${pedido.clienteId}",
                            fecha       = pedido.fecha,
                            totalFinal  = pedido.totalFinal,
                            estadoCobro = pedido.estadoCobro
                        )
                    }

                _uiState.value = DashboardUiState.Success(
                    metrics = DashboardMetrics(
                        pedidosHoy         = pedidosHoy,
                        pendientesFacturar = pendientesFacturar,
                        productosActivos   = productosActivos,
                        ventasMes          = ventasMes
                    ),
                    recentPedidos = recentPedidos
                )
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("Unable to resolve host") == true ->
                        "No se puede conectar al servidor."
                    e.message?.contains("timeout") == true ->
                        "La petición tardó demasiado. Inténtalo de nuevo."
                    else -> e.message ?: "Error desconocido al cargar el dashboard."
                }
                _uiState.value = DashboardUiState.Error(message)
            }
        }
    }
}
