package com.example.distridulce.network.dto

import com.google.gson.annotations.SerializedName

// ── Request ───────────────────────────────────────────────────────────────────

data class LoginRequestDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

// ── Response ──────────────────────────────────────────────────────────────────

/**
 * JWT login response from POST /api/auth/login.
 *
 * Matches the real backend contract (RJMA_backend · LoginResponseDto.java):
 *
 * | Kotlin field  | JSON key     | Backend type | Notes                     |
 * |---------------|--------------|--------------|---------------------------|
 * | [token]       | "token"      | String       | Signed JWT                |
 * | [tipo]        | "tipo"       | String       | Always "Bearer"           |
 * | [expiraEn]    | "expiraEn"   | long         | Expiry — epoch millis     |
 * | [username]    | "username"   | String       | Login name                |
 * | [nombre]      | "nombre"     | String       | Display / full name       |
 * | [rol]         | "rol"        | Rol enum     | "VENDEDOR" or "ADMIN"     |
 *
 * All fields are nullable so Gson never throws if the server shape changes.
 * [token] is the only one consumed as non-null — the app will fail fast on
 * missing token (Result.failure propagated by AuthRepository → LoginViewModel).
 */
data class LoginResponseDto(
    @SerializedName("token")    val token:    String,
    @SerializedName("tipo")     val tipo:     String? = null,
    @SerializedName("expiraEn") val expiraEn: Long?   = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("nombre")   val nombre:   String? = null,
    @SerializedName("rol")      val rol:      String? = null
)
