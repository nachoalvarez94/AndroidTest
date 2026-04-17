package com.example.distridulce.model

// ── Data models ───────────────────────────────────────────────────────────────

/**
 * A single purchasable format for a product (e.g. "Por Unidades", "Por Cajas").
 */
data class ProductOption(
    val id: String,
    val label: String,        // "Por Cajas"
    val description: String,  // "Caja 12 paquetes"
    val price: Double,
    val unit: String = "ud."  // label used in cart: "caja", "bandeja", "paq." …
)

/**
 * A product that can be added to an order, with one or more purchase options.
 */
data class OrderableProduct(
    val id: String,
    val name: String,
    val category: String,
    val options: List<ProductOption>
)

/**
 * One line in the shopping cart.
 * [key] is stable and unique: used for increment / decrement / remove lookups.
 *
 * [articuloId] is the backend Long ID. It is populated when the item comes from
 * a real backend product; null for items sourced from the current mock catalogue.
 * The [PedidoMapper] uses it for the POST /api/pedidos request.
 */
data class CartItem(
    val productId: String,
    val productName: String,
    val option: ProductOption,
    val quantity: Int = 1,
    val articuloId: Long? = null
) {
    val key: String get() = "$productId::${option.id}"
    val lineTotal: Double get() = option.price * quantity
}

// ── Backend → OrderBuilder mapper ────────────────────────────────────────────

/**
 * Maps a real [CatalogProduct] loaded from the backend to the [OrderableProduct]
 * model used by [OrderBuilderScreen].
 *
 * The backend exposes a single price per article, so a single "Por Unidades"
 * [ProductOption] is created.  [CartItem.articuloId] is derived from [CatalogProduct.id],
 * which is already the backend Long serialised as a string.
 */
fun CatalogProduct.toOrderableProduct(): OrderableProduct = OrderableProduct(
    id       = id,
    name     = name,
    category = category,
    options  = listOf(
        ProductOption(
            id          = "$id-U",
            label       = "Por Unidades",
            description = description.ifBlank { "1 ud." },
            price       = price,
            unit        = "ud."
        )
    )
)

// ── Mock catalogue (kept as offline fallback — not used in the main flow) ─────

val sampleOrderProducts: List<OrderableProduct> = listOf(
    // ── Galletas ──────────────────────────────────────────────────────────────
    OrderableProduct("P001", "Galletas María", "Galletas", listOf(
        ProductOption("P001-P", "Por Paquetes", "Paquete 200 g",    1.20, "paq."),
        ProductOption("P001-C", "Por Cajas",    "Caja 12 paquetes", 12.00, "caja"),
    )),
    OrderableProduct("P002", "Galletas Oreo", "Galletas", listOf(
        ProductOption("P002-P", "Por Paquetes", "Paquete 154 g",    1.80, "paq."),
        ProductOption("P002-C", "Por Cajas",    "Caja 10 paquetes", 15.00, "caja"),
    )),
    OrderableProduct("P003", "Galletas Digestive", "Galletas", listOf(
        ProductOption("P003-P", "Por Paquetes", "Paquete 400 g",   2.10, "paq."),
        ProductOption("P003-C", "Por Cajas",    "Caja 8 paquetes", 14.50, "caja"),
    )),
    OrderableProduct("P004", "Galletas Mantequilla", "Galletas", listOf(
        ProductOption("P004-P", "Por Paquetes", "Paquete 200 g",   2.50, "paq."),
        ProductOption("P004-C", "Por Cajas",    "Caja 8 paquetes", 18.00, "caja"),
    )),
    // ── Bollería ──────────────────────────────────────────────────────────────
    OrderableProduct("P005", "Croissant Clásico", "Bollería", listOf(
        ProductOption("P005-U", "Por Unidades",  "1 ud.",           0.80, "ud."),
        ProductOption("P005-B", "Por Bandejas",  "Bandeja 12 uds",  8.00, "bandeja"),
    )),
    OrderableProduct("P006", "Napolitana Chocolate", "Bollería", listOf(
        ProductOption("P006-U", "Por Unidades", "1 ud.",           1.10, "ud."),
        ProductOption("P006-B", "Por Bandejas", "Bandeja 8 uds",   7.50, "bandeja"),
    )),
    OrderableProduct("P007", "Donut Glaseado", "Bollería", listOf(
        ProductOption("P007-U", "Por Unidades", "1 ud.",          0.90, "ud."),
        ProductOption("P007-C", "Por Cajas",    "Caja 6 uds",     4.50, "caja"),
    )),
    OrderableProduct("P008", "Palmera Hojaldre", "Bollería", listOf(
        ProductOption("P008-U", "Por Unidades", "1 ud.",          1.50, "ud."),
        ProductOption("P008-B", "Por Bandejas", "Bandeja 6 uds",  7.50, "bandeja"),
    )),
    // ── Dulces ────────────────────────────────────────────────────────────────
    OrderableProduct("P009", "Turrón de Almendra", "Dulces", listOf(
        ProductOption("P009-U", "Por Tableta", "Tableta 300 g",      4.50, "ud."),
        ProductOption("P009-C", "Por Caja",    "Caja 6 tabletas",   24.00, "caja"),
    )),
    OrderableProduct("P010", "Bombones Surtidos", "Dulces", listOf(
        ProductOption("P010-U", "Caja Individual", "24 bombones",   8.90, "caja"),
        ProductOption("P010-B", "Bandeja Surtida", "48 bombones",  15.00, "bandeja"),
    )),
    OrderableProduct("P011", "Caramelos Surtidos", "Dulces", listOf(
        ProductOption("P011-B", "Por Bolsas", "Bolsa 200 g",        1.20, "bolsa"),
        ProductOption("P011-C", "Por Cajas",  "Caja 12 bolsas",    12.00, "caja"),
    )),
    OrderableProduct("P012", "Gominolas de Fresa", "Dulces", listOf(
        ProductOption("P012-B", "Por Bolsas", "Bolsa 250 g",        1.80, "bolsa"),
        ProductOption("P012-C", "Por Cajas",  "Caja 10 bolsas",    15.00, "caja"),
    )),
)
