package com.example.distridulce.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.distridulce.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
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

    private fun parseError(e: Throwable): String = when (e) {
        is HttpException -> when (e.code()) {
            401  -> "Usuario o contraseña incorrectos."
            403  -> "Acceso denegado."
            500  -> "Error en el servidor. Inténtalo más tarde."
            else -> "Error del servidor (${e.code()})."
        }
        is UnknownHostException  -> "No se puede conectar al servidor.\nVerifica la URL en RetrofitClient."
        is SocketTimeoutException -> "La petición tardó demasiado. Inténtalo de nuevo."
        else -> e.message ?: "Error desconocido."
    }
}
