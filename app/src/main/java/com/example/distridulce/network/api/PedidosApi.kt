package com.example.distridulce.network.api

import com.example.distridulce.network.dto.PedidoRequestDto
import com.example.distridulce.network.dto.PedidoResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit interface for the /api/pedidos endpoints.
 */
interface PedidosApi {

    /** Returns all orders. */
    @GET("api/pedidos")
    suspend fun getPedidos(): List<PedidoResponseDto>

    /** Returns a single order by its primary key. */
    @GET("api/pedidos/{id}")
    suspend fun getPedidoById(@Path("id") id: Long): PedidoResponseDto

    /** Creates a new order and returns the persisted entity with its backend ID. */
    @POST("api/pedidos")
    suspend fun createPedido(@Body request: PedidoRequestDto): PedidoResponseDto
}
