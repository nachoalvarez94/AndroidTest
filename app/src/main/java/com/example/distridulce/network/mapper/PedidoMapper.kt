package com.example.distridulce.network.mapper

import com.example.distridulce.model.CartItem
import com.example.distridulce.model.PaymentSummary
import com.example.distridulce.network.dto.PedidoLineaRequestDto
import com.example.distridulce.network.dto.PedidoRequestDto

/**
 * Maps a list of [CartItem]s (plus optional payment info) to the
 * [PedidoRequestDto] that the backend expects for POST /api/pedidos.
 *
 * ## articuloId resolution
 *
 * Each [CartItem] carries an optional [CartItem.articuloId] set when the item
 * was added from a real backend product.  For legacy mock products ("P001"…)
 * the [CartItem.productId] contains a leading letter; we strip non-digit
 * characters and parse the remainder as a fallback ("P001" → 1).  Items for
 * which no numeric ID can be derived are silently skipped.
 *
 * ## Payment mapping
 *
 * | PaymentSummary state          | estadoCobro  | importeCobrado  |
 * |-------------------------------|--------------|-----------------|
 * | pendingAmount == 0            | "COMPLETO"   | totalAmount      |
 * | paidAmount == 0               | "PENDIENTE"  | 0.0             |
 * | paidAmount > 0, pending > 0   | "PARCIAL"    | paidAmount      |
 */
fun List<CartItem>.toPedidoRequest(
    clienteId: Long,
    observaciones: String? = null,
    paymentSummary: PaymentSummary? = null
): PedidoRequestDto {
    val lineas = mapNotNull { item ->
        val articuloId = item.articuloId
            ?: item.productId.filter { it.isDigit() }.toLongOrNull()
            ?: return@mapNotNull null   // skip unmappable items
        PedidoLineaRequestDto(
            articuloId = articuloId,
            cantidad   = item.quantity.toDouble()
        )
    }

    val estadoCobro: String?
    val importeCobrado: Double?
    if (paymentSummary != null) {
        estadoCobro = when {
            paymentSummary.pendingAmount == 0.0 -> "COMPLETO"
            paymentSummary.paidAmount    == 0.0 -> "PENDIENTE"
            else                                -> "PARCIAL"
        }
        importeCobrado = paymentSummary.paidAmount
    } else {
        estadoCobro    = null
        importeCobrado = null
    }

    return PedidoRequestDto(
        clienteId      = clienteId,
        observaciones  = observaciones,
        lineas         = lineas,
        importeCobrado = importeCobrado,
        estadoCobro    = estadoCobro
    )
}
