package com.example.distridulce.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Raw DTO received from GET /api/articulos and GET /api/articulos/{id}.
 * Field names match the Spring Boot JSON response exactly.
 */
data class ArticuloDto(
    @SerializedName("id")             val id: Long,
    @SerializedName("nombre")         val nombre: String,
    @SerializedName("precio")         val precio: Double,
    @SerializedName("codigoInterno")  val codigoInterno: String?,
    @SerializedName("codigoBarras")   val codigoBarras: String?,
    @SerializedName("activo")         val activo: Boolean,
    /** Possible values from the backend enum: UNIDAD · CAJA · GRANEL · PESO */
    @SerializedName("unidadVenta")    val unidadVenta: String?
)
