package com.example.distridulce.model

/**
 * UI model for a product shown in [CatalogScreen].
 *
 * Replaces the former private `Product` data class that lived inside CatalogScreen.kt.
 * Now shared between the screen, the mapper, and the ViewModel.
 *
 * Fields populated from the backend:
 *   id, name, description, price, unidadVenta, codigoInterno, codigoBarras
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
    val stock: Int = STOCK_UNKNOWN,
    /** Backend enum value: UNIDAD · CAJA · GRANEL · PESO. Null if the backend omits it. */
    val unidadVenta: String? = null,
    /** Raw internal reference code — used for search matching. */
    val codigoInterno: String? = null,
    /** Raw EAN/barcode — used for search matching. */
    val codigoBarras: String? = null
) {
    companion object {
        /** Sentinel value: stock data is not available from the backend. */
        const val STOCK_UNKNOWN = -1
    }
}

// ── Unit label helpers ────────────────────────────────────────────────────────

/**
 * Returns the short display label for a raw [unidadVenta] enum string.
 *
 * | Backend value | Display |
 * |---------------|---------|
 * | UNIDAD        | ud.     |
 * | CAJA          | caja    |
 * | GRANEL        | granel  |
 * | PESO          | kg      |
 * | null / other  | ud.     |
 *
 * Exposed as a standalone function so screens that receive individual fields
 * (e.g. [ProductCard] in CatalogScreen) can call it without a full model.
 */
fun unitLabelFor(unidadVenta: String?): String = when (unidadVenta) {
    "UNIDAD" -> "ud."
    "CAJA"   -> "caja"
    "GRANEL" -> "granel"
    "PESO"   -> "kg"
    else     -> "ud."
}

/** Convenience extension — delegates to [unitLabelFor]. */
fun CatalogProduct.unitLabel(): String = unitLabelFor(unidadVenta)
