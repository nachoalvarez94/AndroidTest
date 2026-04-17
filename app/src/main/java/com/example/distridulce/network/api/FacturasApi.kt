package com.example.distridulce.network.api

import com.example.distridulce.network.dto.FacturaResponseDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit interface for the /api/facturas endpoints.
 */
interface FacturasApi {

    /**
     * Generates a new invoice from an existing order.
     * The backend derives all fiscal data (lines, totals, IVA) from the order.
     * Calling this twice for the same [pedidoId] is expected to be idempotent
     * on a well-behaved backend; the app additionally guards with [OrderSession.facturaId].
     */
    @POST("api/facturas/desde-pedido/{pedidoId}")
    suspend fun createFacturaFromPedido(@Path("pedidoId") pedidoId: Long): FacturaResponseDto

    /** Returns a single invoice by its primary key. */
    @GET("api/facturas/{id}")
    suspend fun getFactura(@Path("id") id: Long): FacturaResponseDto

    /** Returns all invoices. */
    @GET("api/facturas")
    suspend fun getFacturas(): List<FacturaResponseDto>
}
