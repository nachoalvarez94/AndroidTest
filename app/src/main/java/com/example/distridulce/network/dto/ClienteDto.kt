package com.example.distridulce.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Raw DTO received from GET /api/clientes and GET /api/clientes/{id}.
 * Field names match the Spring Boot JSON response exactly.
 */
data class ClienteDto(
    @SerializedName("id")               val id: Long,
    @SerializedName("nombre")           val nombre: String,
    @SerializedName("direccion")        val direccion: String?,
    @SerializedName("email")            val email: String?,
    @SerializedName("telefono")         val telefono: String?,
    @SerializedName("nombreComercio")   val nombreComercio: String?,
    @SerializedName("poblacion")        val poblacion: String?,
    @SerializedName("municipio")        val municipio: String?,
    @SerializedName("documentoFiscal")  val documentoFiscal: String?,
    @SerializedName("activo")           val activo: Boolean
)
