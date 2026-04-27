package com.example.distridulce.network.interceptor

import com.example.distridulce.session.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp application interceptor that handles JWT authentication.
 *
 * ## On every outgoing request
 * If [SessionManager.token] is non-null, injects the header:
 * ```
 * Authorization: Bearer <token>
 * ```
 * No header is added when there is no token (e.g. during login itself).
 *
 * ## On every incoming response
 * If the server returns HTTP 401, the session is cleared synchronously so that
 * [SessionManager.isLoggedIn] immediately becomes `false`.  [DistriDulceApp]
 * observes this and switches to the login screen automatically.
 *
 * No navigation code lives here — the interceptor is UI-agnostic.
 *
 * ## Interceptor order in OkHttpClient
 * This interceptor is added BEFORE the logging interceptor so that the
 * Authorization header is visible in the HTTP logs (useful for debugging).
 * On the response path the order is reversed: logging runs first, then this
 * interceptor checks the status code.
 */
class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        // ── Outgoing request — add Bearer token if available ──────────────────
        val originalRequest = chain.request()
        val token           = SessionManager.token

        val request = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        // ── Proceed ───────────────────────────────────────────────────────────
        val response = chain.proceed(request)

        // ── Incoming response — handle 401 ────────────────────────────────────
        // Skip session clearing for the login endpoint itself:
        //   • The backend returns 400 (not 401) for bad credentials, so a 401
        //     here would be an edge case — but more importantly, calling
        //     clearSession() during login would race with the ViewModel's own
        //     error handling and could reset state the user hasn't seen yet.
        //   • All other endpoints are protected; a 401 there means the token
        //     has expired or been revoked → clear the session and go to login.
        val isLoginRequest = originalRequest.url.encodedPath.endsWith("/api/auth/login")
        if (response.code == 401 && !isLoginRequest) {
            // runBlocking is acceptable here: we are already on an OkHttp IO
            // thread (not a coroutine dispatcher), so there is no deadlock risk.
            // The DataStore write is a single small key removal — very fast.
            runBlocking { SessionManager.clearSession() }
        }

        return response
    }
}
