package com.example.distridulce.network.api

import com.example.distridulce.network.dto.ClienteDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit interface for the /api/clientes endpoints.
 */
interface ClientesApi {

    /** Returns all clients. */
    @GET("api/clientes")
    suspend fun getClientes(): List<ClienteDto>

    /** Returns a single client by its ID. */
    @GET("api/clientes/{id}")
    suspend fun getClienteById(@Path("id") id: Long): ClienteDto
}
