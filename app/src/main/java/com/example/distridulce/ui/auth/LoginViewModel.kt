package com.example.distridulce.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.distridulce.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// ── UI state ──────────────────────────────────────────────────────────────────

sealed class LoginUiState {
    /** Form ready for input. */
    object Idle    : LoginUiState()
    /** Network call in progress — disable the button. */
    object Loading : LoginUiState()
    /** Login failed — show [message] to the user. */
    data class Error(val message: String) : LoginUiState()
    // No Success state: on success SessionManager.isLoggedIn → true and
    // DistriDulceApp switches to the main app automatically.
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Attempts to log in with the provided credentials.
     *
     * On success: [AuthRepository] calls [SessionManager.saveToken], which
     * flips [SessionManager.isLoggedIn] to `true`.  [DistriDulceApp] observes
     * this and replaces [LoginScreen] with the main app — no navigation needed here.
     *
     * On failure: updates [uiState] to [LoginUiState.Error] with a user-facing
     * message derived from the exception type.
     */
    fun login(username: String, password: String) {
        if (_uiState.value is LoginUiState.Loading) return   // prevent double-tap

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            AuthRepository.login(username, password)
                .onSuccess {
                    // Reset state before the screen disappears so that if the
                    // user logs out later and the screen reappears, it shows Idle.
                    _uiState.value = LoginUiState.Idle
                }
                .onFailure { e ->
                    _uiState.value = LoginUiState.Error(parseError(e))
                }
        }
    }

    /**
     * Maps a network/HTTP exception to a short, user-facing Spanish message.
     *
     * HTTP error mapping (matches RJMA_backend GlobalExceptionHandler):
     * - 400 → bad credentials (backend wraps BadCredentialsException as 400)
     * - 401 → kept as fallback in case the backend contract ever changes
     * - 403 → authenticated but forbidden
     * - 5xx → generic server error (range covers 500–599)
     *
     * Network error mapping:
     * - UnknownHostException / no route → no connectivity or wrong host
     * - SocketTimeoutException          → server unreachable / too slow
     * - other IOException               → generic connectivity problem
     */
    private fun parseError(e: Throwable): String = when (e) {
        is HttpException -> when (e.code()) {
            400, 401     -> "Usuario o contraseña incorrectos."
            403          -> "Acceso denegado."
            in 500..599  -> "Error del servidor. Inténtalo más tarde."
            else         -> "Error inesperado (${e.code()})."
        }
        is UnknownHostException   -> "Sin conexión. Verifica tu red."
        is SocketTimeoutException -> "El servidor no responde. Inténtalo de nuevo."
        is IOException            -> "Error de conexión. Inténtalo de nuevo."
        else                      -> "Error desconocido."
    }
}
