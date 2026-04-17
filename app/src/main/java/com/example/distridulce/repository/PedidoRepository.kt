package com.example.distridulce.repository

import com.example.distridulce.model.CartItem
import com.example.distridulce.network.RetrofitClient
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
        observaciones: String? = null
    ): PedidoResponseDto = api.createPedido(
        cartItems.toPedidoRequest(clienteId, observaciones)
    )
}
