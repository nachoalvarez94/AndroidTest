package com.example.distridulce.network.dto

import com.google.gson.annotations.SerializedName

// ── Request ───────────────────────────────────────────────────────────────────

data class LoginRequestDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

// ── Response ──────────────────────────────────────────────────────────────────

/**
 * JWT response from POST /api/auth/login.
 *
 * The default field name is "token" — the most common Spring Boot / Spring
 * Security JWT pattern.
 *
 * ⚠️  If your backend returns a different field name, update @SerializedName:
 *   - Spring Security default response:  "accessToken"
 *   - Older tutorials:                   "jwt"
 *   - Snake-case APIs:                   "access_token"
 */
data class LoginResponseDto(
    @SerializedName("token") val token: String
)
