package com.example.distridulce.repository

import com.example.distridulce.network.RetrofitClient
import com.example.distridulce.network.dto.FacturaResponseDto

/**
 * Single source of truth for invoice (factura) operations.
 *
 * All functions are `suspend` — call them from a coroutine or [viewModelScope].
 */
object FacturaRepository {

    private val api = RetrofitClient.facturasApi

    /** Creates (or retrieves) the invoice for the given [pedidoId]. */
    suspend fun createFacturaFromPedido(pedidoId: Long): FacturaResponseDto =
        api.createFacturaFromPedido(pedidoId)

    /** Fetches an already-existing invoice by its primary key. */
    suspend fun getFactura(id: Long): FacturaResponseDto =
        api.getFactura(id)

    /** Returns all invoices. */
    suspend fun getFacturas(): List<FacturaResponseDto> =
        api.getFacturas()
}
