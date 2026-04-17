package com.example.distridulce.network.dto

import com.google.gson.annotations.SerializedName

// ── Request DTOs ──────────────────────────────────────────────────────────────

data class PedidoLineaRequestDto(
    @SerializedName("articuloId") val articuloId: Long,
    @SerializedName("cantidad")   val cantidad: Double
)

data class PedidoRequestDto(
    @SerializedName("clienteId")      val clienteId: Long,
    @SerializedName("observaciones")  val observaciones: String?,
    @SerializedName("lineas")         val lineas: List<PedidoLineaRequestDto>,
    /** Amount actually collected at order time (0.0 when payment is deferred). */
    @SerializedName("importeCobrado") val importeCobrado: Double?,
    /**
     * Payment status sent to the backend:
     *   "COMPLETO"  — full payment received
     *   "PARCIAL"   — partial payment received; balance pending
     *   "PENDIENTE" — no payment received yet
     */
    @SerializedName("estadoCobro")    val estadoCobro: String?
)

// ── Response DTOs ─────────────────────────────────────────────────────────────

data class PedidoResponseDto(
    @SerializedName("id")               val id: Long,
    @SerializedName("numero")           val numero: Long,
    @SerializedName("clienteId")        val clienteId: Long,
    @SerializedName("estado")           val estado: String,
    @SerializedName("observaciones")    val observaciones: String?,
    @SerializedName("totalBruto")       val totalBruto: Double,
    @SerializedName("totalDescuento")   val totalDescuento: Double,
    @SerializedName("totalFinal")       val totalFinal: Double,
    @SerializedName("fecha")            val fecha: String,
    @SerializedName("createdAt")        val createdAt: String,
    @SerializedName("updatedAt")        val updatedAt: String,
    @SerializedName("lineas")           val lineas: List<PedidoLineaResponseDto>,
    /**
     * Payment status persisted by the backend:
     *   "COMPLETO"  — full payment received
     *   "PARCIAL"   — partial payment received; balance pending
     *   "PENDIENTE" — no payment received yet
     *   null        — legacy record created before this field existed
     */
    @SerializedName("estadoCobro")      val estadoCobro: String?,
    /** Amount actually collected at order time. Null for legacy records. */
    @SerializedName("importeCobrado")   val importeCobrado: Double?,
    /** Remaining balance still owed. Null for legacy records. */
    @SerializedName("importePendiente") val importePendiente: Double?
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
