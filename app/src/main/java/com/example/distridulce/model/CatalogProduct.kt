package com.example.distridulce.model

/**
 * UI model for a product shown in [CatalogScreen].
 *
 * Replaces the former private `Product` data class that lived inside CatalogScreen.kt.
 * Now shared between the screen, the mapper, and the ViewModel.
 *
 * Fields populated from the backend:
 *   id, name, description, price
 *
 * Fields NOT yet available from the backend (marked with TODO):
 *   category — defaults to "General" until the API exposes a categoría field.
 *   stock    — defaults to STOCK_UNKNOWN (-1) until the API exposes stock.
 *              The UI hides the stock label when the value is STOCK_UNKNOWN.
 */
data class CatalogProduct(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    val stock: Int = STOCK_UNKNOWN
) {
    companion object {
        /** Sentinel value: stock data is not available from the backend. */
        const val STOCK_UNKNOWN = -1
    }
}
