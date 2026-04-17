package com.example.distridulce.network.mapper

import com.example.distridulce.model.CartItem
import com.example.distridulce.network.dto.PedidoLineaRequestDto
import com.example.distridulce.network.dto.PedidoRequestDto

/**
 * Maps a list of [CartItem]s to the [PedidoRequestDto] that the backend
 * expects for POST /api/pedidos.
 *
 * ## articuloId resolution
 *
 * Each [CartItem] carries an optional [CartItem.articuloId] that is set when
 * the item was added from a real backend product.  For the current mock
 * products ("P001", "P002"…) the [CartItem.productId] contains a string with
 * a leading letter; we strip non-digit characters and parse the remainder as a
 * temporary bridge ("P001" → 1) until OrderBuilderScreen is connected to real
 * articles.  Items for which no numeric ID can be derived are silently skipped.
 */
fun List<CartItem>.toPedidoRequest(
    clienteId: Long,
    observaciones: String? = null
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
    return PedidoRequestDto(
        clienteId     = clienteId,
        observaciones = observaciones,
        lineas        = lineas
    )
}
