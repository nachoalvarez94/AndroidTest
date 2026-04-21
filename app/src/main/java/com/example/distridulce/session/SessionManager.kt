package com.example.distridulce.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.distridulce.model.OrderSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

// ── DataStore ─────────────────────────────────────────────────────────────────

/**
 * Single DataStore instance for the whole app, tied to the applicationContext.
 * The delegate ensures only one instance is created per application process.
 */
private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "session")

// ── SessionManager ────────────────────────────────────────────────────────────

/**
 * Single source of truth for the authentication state.
 *
 * ## Token lifecycle
 * 1. App starts → [DistriDulceApplication] calls [init] then [loadToken].
 * 2. [loadToken] reads DataStore on a background thread and updates [isLoggedIn].
 * 3. Successful login → [AuthRepository] calls [saveToken].
 * 4. Logout or 401 → [clearSession] is called; token is erased from memory and
 *    DataStore, [isLoggedIn] becomes `false`.
 *
 * ## Threading
 * [token] is a `@Volatile` plain property — safe to read from any thread,
 * including OkHttp's IO pool.  [isLoggedIn] is a `MutableStateFlow`, also
 * thread-safe.  All DataStore operations are `suspend` and run on IO dispatchers.
 *
 * ## isLoggedIn states
 * - `null`  — startup: DataStore not read yet; show loading screen.
 * - `true`  — valid JWT in memory; show the main app.
 * - `false` — no token or session cleared; show login screen.
 */
object SessionManager {

    private val TOKEN_KEY = stringPreferencesKey("jwt_token")

    // Set once in init() before any IO starts.
    private lateinit var appContext: Context

    /**
     * The current JWT in memory.
     *
     * Read by [AuthInterceptor] on the OkHttp thread — must be @Volatile.
     * `null` means no active session.
     */
    @Volatile
    var token: String? = null
        private set

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)

    /**
     * Three-valued session state observed by [DistriDulceApp]:
     * - `null`  → initializing (DataStore not read yet)
     * - `true`  → authenticated
     * - `false` → unauthenticated
     */
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    // ── Initialisation ────────────────────────────────────────────────────────

    /**
     * Must be called once from [DistriDulceApplication.onCreate] before any
     * other SessionManager function is used.
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Reads the persisted token from DataStore and updates [token] + [isLoggedIn].
     *
     * Called from the application scope in [DistriDulceApplication.onCreate].
     * Until this completes, [isLoggedIn] remains `null`.
     */
    suspend fun loadToken() {
        val prefs      = appContext.dataStore.data.first()
        val savedToken = prefs[TOKEN_KEY]
        token          = savedToken
        _isLoggedIn.value = savedToken != null
    }

    // ── Session operations ────────────────────────────────────────────────────

    /**
     * Persists [newToken] to DataStore and activates the session.
     *
     * Called by [AuthRepository] immediately after a successful login response.
     * Updates the in-memory [token] first so the interceptor can use it right
     * away, then persists to DataStore for survival across process restarts.
     */
    suspend fun saveToken(newToken: String) {
        token = newToken
        appContext.dataStore.edit { prefs -> prefs[TOKEN_KEY] = newToken }
        _isLoggedIn.value = true
    }

    /**
     * Erases the token from memory and DataStore and deactivates the session.
     *
     * Safe to call from a coroutine context.  Also clears [OrderSession] so
     * no order data leaks to the next user.
     *
     * The UI reacts automatically by observing [isLoggedIn] — no navigation
     * code belongs here.
     */
    suspend fun clearSession() {
        token = null
        OrderSession.clear()
        appContext.dataStore.edit { prefs -> prefs.remove(TOKEN_KEY) }
        _isLoggedIn.value = false
    }
}
