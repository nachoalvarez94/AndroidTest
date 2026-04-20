package com.example.distridulce.network.api

import com.example.distridulce.network.dto.FacturaResponseDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming

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

    /**
     * Downloads the PDF for invoice [facturaId] as a raw byte stream.
     *
     * [@Streaming] tells Retrofit to pass the body directly to the caller without
     * buffering it in memory — essential for binary files of non-trivial size.
     * The caller is responsible for closing [ResponseBody] after reading.
     */
    @Streaming
    @GET("api/facturas/{id}/pdf")
    suspend fun downloadFacturaPdf(@Path("id") facturaId: Long): Response<ResponseBody>
}
