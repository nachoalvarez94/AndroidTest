package com.example.distridulce.network

import com.example.distridulce.network.api.ArticulosApi
import com.example.distridulce.network.api.AuthApi
import com.example.distridulce.network.api.ClientesApi
import com.example.distridulce.network.api.FacturasApi
import com.example.distridulce.network.api.PedidosApi
import com.example.distridulce.network.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client shared across the whole app.
 *
 * ## Configuring BASE_URL
 * | Scenario                       | Value                              |
 * |--------------------------------|------------------------------------|
 * | Android Emulator → localhost   | `"http://10.0.2.2:8080/"`          |
 * | Physical device (same Wi-Fi)   | `"http://192.168.x.x:8080/"`       |
 * | Remote / staging server        | `"https://api.yourdomain.com/"`    |
 *
 * Cleartext HTTP is allowed via `android:usesCleartextTraffic="true"` in the
 * manifest (development only — use HTTPS in production).
 *
 * ## Interceptor order
 * 1. [AuthInterceptor] — adds `Authorization: Bearer` header; handles 401 responses.
 * 2. [HttpLoggingInterceptor] — logs the full request *including* the auth header.
 */
object RetrofitClient {

    // ── Configuration ─────────────────────────────────────────────────────────

    /**
     * Change this to match your backend's address.
     * Default: emulator loopback → Spring Boot on port 8080.
     */
    const val BASE_URL = "http://10.0.2.2:8080/"

    // ── OkHttp ────────────────────────────────────────────────────────────────

    private val authInterceptor = AuthInterceptor()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)    // 1st: injects/validates token
        .addInterceptor(loggingInterceptor) // 2nd: logs full request + response
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ── Retrofit ──────────────────────────────────────────────────────────────

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // ── API instances ─────────────────────────────────────────────────────────

    val authApi: AuthApi       = retrofit.create(AuthApi::class.java)
    val articulosApi: ArticulosApi = retrofit.create(ArticulosApi::class.java)
    val clientesApi: ClientesApi   = retrofit.create(ClientesApi::class.java)
    val facturasApi: FacturasApi   = retrofit.create(FacturasApi::class.java)
    val pedidosApi: PedidosApi     = retrofit.create(PedidosApi::class.java)
}
