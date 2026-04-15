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

    /** Unique reference generated when the invoice is confirmed. */
    var invoiceRef: String = ""

    fun clear() {
        client         = null
        cartItems      = emptyList()
        paymentSummary = null
        invoiceRef     = ""
    }
}
