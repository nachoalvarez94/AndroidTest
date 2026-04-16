package com.example.distridulce.model

// ── Data model ────────────────────────────────────────────────────────────────

data class Client(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    // Extended fields — default values keep existing call-sites compatible.
    val email: String = "",
    val totalOrders: Int = 0,
    val lastOrderText: String = "Sin pedidos"
)

/** Returns up to two initials from the client name (e.g. "Supermercado El Ahorro" → "SE"). */
fun Client.initials(): String {
    val words = name.trim().split(Regex("\\s+"))
    return when {
        words.size >= 2 -> "${words[0].first()}${words[1].first()}"
        words.size == 1 -> words[0].take(2)
        else -> "?"
    }.uppercase()
}

/** Finds a client by id in [mockClients]. Returns null if not found or id is null. */
fun findClientById(id: String?): Client? = mockClients.find { it.id == id }

// ── Mock data ─────────────────────────────────────────────────────────────────

val mockClients = listOf(
    Client(
        id            = "C001",
        name          = "Supermercado El Ahorro",
        address       = "Calle Mayor 12, Madrid",
        phone         = "+34 911 234 567",
        email         = "pedidos@elahorro.es",
        totalOrders   = 34,
        lastOrderText = "Hace 2 días"
    ),
    Client(
        id            = "C002",
        name          = "Tienda La Esquina",
        address       = "Av. Constitución 45, Barcelona",
        phone         = "+34 932 345 678",
        email         = "info@laesquina.cat",
        totalOrders   = 18,
        lastOrderText = "Hace 1 semana"
    ),
    Client(
        id            = "C003",
        name          = "Kiosko Central",
        address       = "Plaza España 3, Valencia",
        phone         = "+34 963 456 789",
        email         = "kioskocentral@gmail.com",
        totalOrders   = 9,
        lastOrderText = "Hace 3 semanas"
    ),
    Client(
        id            = "C004",
        name          = "Minimercado Rápido",
        address       = "C/ Serrano 78, Madrid",
        phone         = "+34 914 567 890",
        email         = "compras@minimercadorapido.es",
        totalOrders   = 27,
        lastOrderText = "Ayer"
    ),
    Client(
        id            = "C005",
        name          = "Bodega del Sur",
        address       = "Paseo Marítimo 22, Málaga",
        phone         = "+34 952 678 901",
        email         = "bodegadelsur@correo.es",
        totalOrders   = 12,
        lastOrderText = "Hace 5 días"
    ),
    Client(
        id            = "C006",
        name          = "Distribuidora Norte",
        address       = "Polígono Industrial Norte, Bilbao",
        phone         = "+34 944 789 012",
        email         = "logistica@distribnorte.com",
        totalOrders   = 51,
        lastOrderText = "Hoy"
    ),
    Client(
        id            = "C007",
        name          = "Almacén Central",
        address       = "C/ Industria 5, Zaragoza",
        phone         = "+34 976 890 123",
        email         = "almacencentral@zaragoza.net",
        totalOrders   = 7,
        lastOrderText = "Hace 1 mes"
    ),
    Client(
        id            = "C008",
        name          = "Colmado La Plaza",
        address       = "Plaza del Mercado 1, Sevilla",
        phone         = "+34 954 901 234",
        email         = "laplaza@colmados.es",
        totalOrders   = 23,
        lastOrderText = "Hace 4 días"
    ),
)
