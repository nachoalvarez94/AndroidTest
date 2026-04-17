package com.example.distridulce.repository

import com.example.distridulce.model.CartItem
import com.example.distridulce.model.PaymentSummary
import com.example.distridulce.network.RetrofitClient
import com.example.distridulce.network.dto.PedidoRequestDto
import com.example.distridulce.network.dto.PedidoResponseDto
import com.example.distridulce.network.mapper.toPedidoRequest

/**
 * Single source of truth for order (pedido) operations.
 *
 * All functions are `suspend` — call them from a coroutine or [viewModelScope].
 */
object PedidoRepository {

    private val api = RetrofitClient.pedidosApi

    /** Fetches all orders from the backend. */
    suspend fun getPedidos(): List<PedidoResponseDto> = api.getPedidos()

    /** Fetches a single order by its backend primary key. */
    suspend fun getPedidoById(id: Long): PedidoResponseDto = api.getPedidoById(id)

    /**
     * Sends [cartItems] as a new order for [clienteId] to the backend.
     * Returns the full [PedidoResponseDto] on success (including the backend-
     * assigned [PedidoResponseDto.id] and [PedidoResponseDto.numero]).
     */
    suspend fun createPedido(
        clienteId: Long,
        cartItems: List<CartItem>,
        paymentSummary: PaymentSummary? = null,
        observaciones: String? = null
    ): PedidoResponseDto = api.createPedido(
        cartItems.toPedidoRequest(clienteId, observaciones, paymentSummary)
    )

    /**
     * Updates an existing order via PUT /api/pedidos/{id}.
     *
     * The [request] is built directly by the caller (typically [EditOrderViewModel])
     * with `estadoCobro = null` so the backend recalculates it from [importeCobrado]
     * and the order total.
     */
    suspend fun updatePedido(id: Long, request: PedidoRequestDto): PedidoResponseDto =
        api.updatePedido(id, request)
}
