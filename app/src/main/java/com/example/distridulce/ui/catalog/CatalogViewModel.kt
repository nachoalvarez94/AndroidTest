package com.example.distridulce.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.distridulce.model.CatalogProduct
import com.example.distridulce.repository.ArticuloRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI state ──────────────────────────────────────────────────────────────────

sealed class CatalogUiState {
    /** Initial load in progress. */
    object Loading : CatalogUiState()

    /**
     * Products loaded successfully.
     * [products] is the full unfiltered list; search and category filtering
     * are applied locally in the composable.
     */
    data class Success(val products: List<CatalogProduct>) : CatalogUiState()

    /** Network or server error. */
    data class Error(val message: String) : CatalogUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

/**
 * Drives [CatalogScreen].
 *
 * Uses [ArticuloRepository] as a singleton — no DI framework yet.
 */
class CatalogViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    /** (Re-)loads products from the backend. Resets state to Loading first. */
    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            try {
                val products = ArticuloRepository.getArticulos()
                _uiState.value = CatalogUiState.Success(products)
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("Unable to resolve host") == true ->
                        "No se puede conectar al servidor.\nVerifica la URL en RetrofitClient."
                    e.message?.contains("timeout") == true ->
                        "La petición tardó demasiado. Inténtalo de nuevo."
                    else ->
                        e.message ?: "Error desconocido al cargar el catálogo."
                }
                _uiState.value = CatalogUiState.Error(message)
            }
        }
    }
}
