package com.example.distridulce.model

// ── Data model ────────────────────────────────────────────────────────────────

data class Client(
    val id: String,
    val name: String,
    val address: String,
    val phone: String
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
        id      = "C001",
        name    = "Supermercado El Ahorro",
        address = "Calle Mayor 12, Madrid",
        phone   = "+34 911 234 567"
    ),
    Client(
        id      = "C002",
        name    = "Tienda La Esquina",
        address = "Av. Constitución 45, Barcelona",
        phone   = "+34 932 345 678"
    ),
    Client(
        id      = "C003",
        name    = "Kiosko Central",
        address = "Plaza España 3, Valencia",
        phone   = "+34 963 456 789"
    ),
    Client(
        id      = "C004",
        name    = "Minimercado Rápido",
        address = "C/ Serrano 78, Madrid",
        phone   = "+34 914 567 890"
    ),
    Client(
        id      = "C005",
        name    = "Bodega del Sur",
        address = "Paseo Marítimo 22, Málaga",
        phone   = "+34 952 678 901"
    ),
    Client(
        id      = "C006",
        name    = "Distribuidora Norte",
        address = "Polígono Industrial Norte, Bilbao",
        phone   = "+34 944 789 012"
    ),
    Client(
        id      = "C007",
        name    = "Almacén Central",
        address = "C/ Industria 5, Zaragoza",
        phone   = "+34 976 890 123"
    ),
    Client(
        id      = "C008",
        name    = "Colmado La Plaza",
        address = "Plaza del Mercado 1, Sevilla",
        phone   = "+34 954 901 234"
    ),
)
