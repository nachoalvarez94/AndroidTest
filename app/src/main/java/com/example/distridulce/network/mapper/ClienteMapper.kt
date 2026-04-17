package com.example.distridulce.network.mapper

import com.example.distridulce.model.Client
import com.example.distridulce.network.dto.ClienteDto

/**
 * Maps a [ClienteDto] from the network layer to the app's [Client] UI model.
 *
 * Fields not available from this endpoint (totalOrders, lastOrderText) are
 * left at their defaults until the orders endpoint is integrated.
 */
fun ClienteDto.toClient(): Client = Client(
    id      = id.toString(),
    name    = buildDisplayName(this),
    address = buildAddress(this),
    phone   = telefono  ?: "",
    email   = email     ?: ""
    // totalOrders and lastOrderText keep their default values (0 / "Sin pedidos")
    // until the /api/pedidos endpoint is connected.
)

/** Prefer the commercial name when present; fall back to the legal name. */
private fun buildDisplayName(dto: ClienteDto): String =
    dto.nombreComercio?.takeIf { it.isNotBlank() } ?: dto.nombre

/**
 * Builds a single address string from the available address parts.
 * Example: "Calle Mayor 5, Sevilla, España"
 */
private fun buildAddress(dto: ClienteDto): String =
    listOfNotNull(dto.direccion, dto.poblacion, dto.municipio)
        .filter { it.isNotBlank() }
        .joinToString(", ")
