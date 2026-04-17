package com.example.distridulce.network.api

import com.example.distridulce.network.dto.ArticuloDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit interface for the /api/articulos endpoints.
 */
interface ArticulosApi {

    /** Returns all articles. Inactive items are included — filter by [ArticuloDto.activo] as needed. */
    @GET("api/articulos")
    suspend fun getArticulos(): List<ArticuloDto>

    /** Returns a single article by its ID. */
    @GET("api/articulos/{id}")
    suspend fun getArticuloById(@Path("id") id: Long): ArticuloDto
}
