package com.example.distridulce.network.mapper

import com.example.distridulce.model.CatalogProduct
import com.example.distridulce.network.dto.ArticuloDto

/**
 * Maps an [ArticuloDto] from the network layer to the app's [CatalogProduct] UI model.
 *
 * ## Fields pending backend support
 *
 * | UI field   | Status                                              |
 * |------------|-----------------------------------------------------|
 * | category   | TODO: add `categoria` field to ArticuloDto          |
 * | stock      | TODO: add `stock` field to ArticuloDto              |
 *
 * Until those fields are available, `category` defaults to "General" and
 * `stock` is set to [CatalogProduct.STOCK_UNKNOWN] so the UI hides it cleanly.
 *
 * Fields now mapped from the backend: [ArticuloDto.unidadVenta],
 * [ArticuloDto.codigoInterno], [ArticuloDto.codigoBarras].
 */
fun ArticuloDto.toCatalogProduct(): CatalogProduct = CatalogProduct(
    id            = id.toString(),
    name          = nombre,
    description   = buildDescription(this),
    // TODO: replace with dto.categoria once the backend exposes it
    category      = "General",
    price         = precio,
    // TODO: replace with dto.stock once the backend exposes it
    stock         = CatalogProduct.STOCK_UNKNOWN,
    unidadVenta   = unidadVenta,
    codigoInterno = codigoInterno,
    codigoBarras  = codigoBarras
)

/**
 * Builds a human-readable description from the available code fields.
 * Prefers [ArticuloDto.codigoInterno]; falls back to barcode; empty string if neither.
 */
private fun buildDescription(dto: ArticuloDto): String = when {
    !dto.codigoInterno.isNullOrBlank() -> "Ref. ${dto.codigoInterno}"
    !dto.codigoBarras.isNullOrBlank()  -> "EAN ${dto.codigoBarras}"
    else                               -> ""
}
