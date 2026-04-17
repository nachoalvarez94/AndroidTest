package com.example.distridulce.repository

import com.example.distridulce.model.Client
import com.example.distridulce.network.RetrofitClient
import com.example.distridulce.network.mapper.toClient

/**
 * Single source of truth for client data.
 *
 * Currently wraps the Retrofit API directly.
 * When caching / Room is added later, it will be the only file that changes.
 *
 * All functions are `suspend` — call them from a coroutine or [viewModelScope].
 */
object ClienteRepository {

    private val api = RetrofitClient.clientesApi

    /** Fetches and maps all clients from the backend. */
    suspend fun getClientes(): List<Client> =
        api.getClientes().map { it.toClient() }

    /** Fetches and maps a single client by its numeric backend ID. */
    suspend fun getClienteById(id: Long): Client =
        api.getClienteById(id).toClient()
}
