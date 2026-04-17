package com.example.distridulce.network.dto

import com.google.gson.annotations.SerializedName

// ── Request DTOs ──────────────────────────────────────────────────────────────

data class PedidoLineaRequestDto(
    @SerializedName("articuloId") val articuloId: Long,
    @SerializedName("cantidad")   val cantidad: Double
)

data class PedidoRequestDto(
    @SerializedName("clienteId")     val clienteId: Long,
    @SerializedName("observaciones") val observaciones: String?,
    @SerializedName("lineas")        val lineas: List<PedidoLineaRequestDto>
)

// ── Response DTOs ─────────────────────────────────────────────────────────────

data class PedidoResponseDto(
    @SerializedName("id")             val id: Long,
    @SerializedName("numero")         val numero: Long,
    @SerializedName("clienteId")      val clienteId: Long,
    @SerializedName("estado")         val estado: String,
    @SerializedName("observaciones")  val observaciones: String?,
    @SerializedName("totalBruto")     val totalBruto: Double,
    @SerializedName("totalDescuento") val totalDescuento: Double,
    @SerializedName("totalFinal")     val totalFinal: Double,
    @SerializedName("fecha")          val fecha: String,
    @SerializedName("createdAt")      val createdAt: String,
    @SerializedName("updatedAt")      val updatedAt: String,
    @SerializedName("lineas")         val lineas: List<PedidoLineaResponseDto>
)

data class PedidoLineaResponseDto(
    @SerializedName("id")             val id: Long,
    @SerializedName("articuloId")     val articuloId: Long?,
    @SerializedName("nombreArticulo") val nombreArticulo: String,
    @SerializedName("precioUnitario") val precioUnitario: Double,
    @SerializedName("cantidad")       val cantidad: Double,
    @SerializedName("subtotal")       val subtotal: Double,
    @SerializedName("descuento")      val descuento: Double,
    @SerializedName("totalLinea")     val totalLinea: Double
)
