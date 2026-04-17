package com.example.distridulce.model

// ── Payment method ────────────────────────────────────────────────────────────

enum class PaymentMethod {
    FULL,    // Pago Completo
    PARTIAL, // Pago Parcial
    PENDING  // Pago Pendiente
}

// ── Payment summary ───────────────────────────────────────────────────────────

/**
 * Immutable snapshot of all payment-related figures for a confirmed order.
 * Created in [CheckoutScreen] and consumed by [InvoiceScreen].
 */
data class PaymentSummary(
    val totalAmount: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val paymentStatus: String
) {
    companion object {
        fun full(total: Double) = PaymentSummary(
            totalAmount   = total,
            paidAmount    = total,
            pendingAmount = 0.0,
            paymentStatus = "Pagado Completo"
        )

        fun partial(total: Double, paid: Double) = PaymentSummary(
            totalAmount   = total,
            paidAmount    = paid,
            pendingAmount = total - paid,
            paymentStatus = "Pago Parcial"
        )

        fun pending(total: Double) = PaymentSummary(
            totalAmount   = total,
            paidAmount    = 0.0,
            pendingAmount = total,
            paymentStatus = "Pendiente"
        )
    }
}

// ── In-memory order session ───────────────────────────────────────────────────

/**
 * Lightweight singleton that carries the active order across the
 * OrderBuilder → Checkout → Invoice navigation chain.
 *
 * No persistence: data lives only as long as the process is alive.
 * Call [clear] when starting a new order.
 */
object OrderSession {
    var client: Client? = null
    var cartItems: List<CartItem> = emptyList()
    var paymentSummary: PaymentSummary? = null

    /** Human-readable invoice reference shown on the invoice screen. */
    var invoiceRef: String = ""

    /** Backend-assigned pedido primary key (available after a successful POST /api/pedidos). */
    var pedidoId: Long? = null

    /** Backend-assigned sequential order number. */
    var pedidoNumero: Long? = null

    /**
     * Backend-assigned factura primary key (set after InvoiceViewModel successfully
     * calls POST /api/facturas/desde-pedido/{pedidoId}).
     * Used as a guard: if set, InvoiceViewModel uses GET instead of POST to avoid
     * creating a duplicate invoice when the screen is revisited.
     */
    var facturaId: Long? = null

    /** Backend-assigned sequential invoice number (e.g. 42 → shown as "FAC-00042"). */
    var facturaNumero: Long? = null

    /**
     * Tracks whether each pedido was fully paid at submission time.
     * Key = backend pedidoId; value = true only when [PaymentMethod.FULL] was selected.
     *
     * This is an in-memory cache so it only holds data for orders created in
     * the current process lifetime.  Orders not present here default to `true`
     * in [OrdersViewModel] (optimistic assumption for historical orders where
     * the backend does not yet return a payment-status field).
     *
     * TODO: replace with a real backend field when the API exposes it.
     */
    val pagoCompleto: MutableMap<Long, Boolean> = mutableMapOf()

    fun clear() {
        client         = null
        cartItems      = emptyList()
        paymentSummary = null
        invoiceRef     = ""
        pedidoId       = null
        pedidoNumero   = null
        facturaId      = null
        facturaNumero  = null
        // NOTE: pagoCompleto is intentionally NOT cleared here — it acts as a
        // cross-session cache. Call pagoCompleto.clear() explicitly if needed.
    }
}
