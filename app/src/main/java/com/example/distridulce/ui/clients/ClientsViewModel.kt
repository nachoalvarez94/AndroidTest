package com.example.distridulce.ui.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.distridulce.model.Client
import com.example.distridulce.repository.ClienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI state ──────────────────────────────────────────────────────────────────

sealed class ClientsUiState {
    /** Initial load in progress. */
    object Loading : ClientsUiState()

    /** Data loaded successfully. [clients] is the full unfiltered list from the backend. */
    data class Success(val clients: List<Client>) : ClientsUiState()

    /** Network or server error. [message] is a human-readable description. */
    data class Error(val message: String) : ClientsUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

/**
 * Drives [ClientsScreen].
 *
 * No DI framework yet — [ClienteRepository] is accessed as a singleton object.
 * A factory can be added later when Hilt is introduced.
 */
class ClientsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ClientsUiState>(ClientsUiState.Loading)
    val uiState: StateFlow<ClientsUiState> = _uiState.asStateFlow()

    init {
        loadClients()
    }

    /** (Re-)loads clients from the backend. Resets state to Loading first. */
    fun loadClients() {
        viewModelScope.launch {
            _uiState.value = ClientsUiState.Loading
            try {
                val clients = ClienteRepository.getClientes()
                _uiState.value = ClientsUiState.Success(clients)
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("Unable to resolve host") == true ->
                        "No se puede conectar al servidor.\nVerifica la URL en RetrofitClient."
                    e.message?.contains("timeout") == true ->
                        "La petición tardó demasiado. Inténtalo de nuevo."
                    else ->
                        e.message ?: "Error desconocido al cargar clientes."
                }
                _uiState.value = ClientsUiState.Error(message)
            }
        }
    }
}
