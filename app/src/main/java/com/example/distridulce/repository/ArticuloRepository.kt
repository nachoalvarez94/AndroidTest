package com.example.distridulce.repository

import com.example.distridulce.model.CatalogProduct
import com.example.distridulce.network.RetrofitClient
import com.example.distridulce.network.mapper.toCatalogProduct

/**
 * Single source of truth for article/product data in the catalogue.
 *
 * Only returns active articles ([ArticuloDto.activo] == true).
 * Inactive items are filtered out here so the UI never has to worry about them.
 *
 * All functions are `suspend` — call them from a coroutine or [viewModelScope].
 */
object ArticuloRepository {

    private val api = RetrofitClient.articulosApi

    /** Fetches, filters (active only), and maps all articles. */
    suspend fun getArticulos(): List<CatalogProduct> =
        api.getArticulos()
            .filter { it.activo }
            .map { it.toCatalogProduct() }

    /** Fetches and maps a single article by its numeric backend ID. */
    suspend fun getArticuloById(id: Long): CatalogProduct =
        api.getArticuloById(id).toCatalogProduct()
}
