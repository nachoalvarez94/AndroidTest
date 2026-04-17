package com.example.distridulce.network

import com.example.distridulce.network.api.ArticulosApi
import com.example.distridulce.network.api.ClientesApi
import com.example.distridulce.network.api.FacturasApi
import com.example.distridulce.network.api.PedidosApi
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
 */
object RetrofitClient {

    // ── Configuration ─────────────────────────────────────────────────────────

    /**
     * Change this to match your backend's address.
     * Default: emulator loopback → Spring Boot on port 8080.
     */
    const val BASE_URL = "http://10.0.2.2:8080/"

    // ── OkHttp ────────────────────────────────────────────────────────────────

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
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

    val articulosApi: ArticulosApi = retrofit.create(ArticulosApi::class.java)
    val clientesApi: ClientesApi   = retrofit.create(ClientesApi::class.java)
    val facturasApi: FacturasApi   = retrofit.create(FacturasApi::class.java)
    val pedidosApi: PedidosApi     = retrofit.create(PedidosApi::class.java)
}
