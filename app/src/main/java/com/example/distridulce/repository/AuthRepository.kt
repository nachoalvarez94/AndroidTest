package com.example.distridulce.repository

import com.example.distridulce.network.RetrofitClient
import com.example.distridulce.network.dto.LoginRequestDto
import com.example.distridulce.session.SessionManager

/**
 * Single source of truth for authentication operations.
 *
 * Wraps [AuthApi] and delegates token persistence to [SessionManager].
 * Returns [Result] so callers never need to catch exceptions directly.
 */
object AuthRepository {

    private val api = RetrofitClient.authApi

    /**
     * Authenticates against POST /api/auth/login.
     *
     * On success: persists the JWT via [SessionManager.saveToken], which sets
     * [SessionManager.isLoggedIn] to `true` and the UI switches to the main app.
     *
     * On failure: returns [Result.failure] with the original exception so the
     * caller (ViewModel) can format a user-friendly message.
     *
     * Common failure causes:
     * - [retrofit2.HttpException] with code 401 → wrong credentials
     * - [java.net.UnknownHostException] → no network / wrong BASE_URL
     * - [java.net.SocketTimeoutException] → server unreachable
     */
    suspend fun login(username: String, password: String): Result<Unit> = runCatching {
        val response = api.login(LoginRequestDto(username, password))
        SessionManager.saveToken(response.token)
    }
}
