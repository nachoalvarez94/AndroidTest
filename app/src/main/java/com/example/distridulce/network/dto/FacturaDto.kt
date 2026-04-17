package com.example.distridulce.network.dto

import com.google.gson.annotations.SerializedName

// ── Response DTOs ─────────────────────────────────────────────────────────────

data class FacturaResponseDto(
    @SerializedName("id")                     val id: Long,
    @SerializedName("numeroFactura")          val numeroFactura: Long,
    @SerializedName("pedidoId")               val pedidoId: Long,
    @SerializedName("clienteId")              val clienteId: Long,
    @SerializedName("nombreCliente")          val nombreCliente: String,
    @SerializedName("direccionCliente")       val direccionCliente: String,
    @SerializedName("emailCliente")           val emailCliente: String?,
    @SerializedName("telefonoCliente")        val telefonoCliente: String?,
    @SerializedName("documentoFiscalCliente") val documentoFiscalCliente: String?,
    @SerializedName("fechaEmision")           val fechaEmision: String,
    @SerializedName("estado")                 val estado: String,
    @SerializedName("baseImponible")          val baseImponible: Double,
    @SerializedName("impuestos")              val impuestos: Double,
    @SerializedName("total")                  val total: Double,
    @SerializedName("createdAt")              val createdAt: String,
    @SerializedName("updatedAt")              val updatedAt: String,
    @SerializedName("lineas")                 val lineas: List<FacturaLineaResponseDto>
)

data class FacturaLineaResponseDto(
    @SerializedName("id")             val id: Long,
    @SerializedName("articuloId")     val articuloId: Long?,
    @SerializedName("nombreArticulo") val nombreArticulo: String,
    @SerializedName("codigoArticulo") val codigoArticulo: String?,
    @SerializedName("precioUnitario") val precioUnitario: Double,
    @SerializedName("cantidad")       val cantidad: Double,
    @SerializedName("subtotal")       val subtotal: Double,
    @SerializedName("tipoIva")        val tipoIva: Double,
    @SerializedName("cuotaIva")       val cuotaIva: Double,
    @SerializedName("totalLinea")     val totalLinea: Double
)
