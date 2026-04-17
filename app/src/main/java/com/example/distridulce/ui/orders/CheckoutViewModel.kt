package com.example.distridulce.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.distridulce.model.CartItem
import com.example.distridulce.model.OrderSession
import com.example.distridulce.model.PaymentSummary
import com.example.distridulce.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── Submit state ──────────────────────────────────────────────────────────────

sealed class SubmitState {
    /** Nothing happening — button is interactive. */
    object Idle : SubmitState()

    /** Network request in-flight — button shows spinner and is disabled. */
    object Submitting : SubmitState()

    /** Backend returned an error — show inline error banner. */
    data class Error(val message: String) : SubmitState()

    /**
     * Order was created successfully.
     * [CheckoutScreen] observes this via [LaunchedEffect] and triggers navigation.
     */
    object Success : SubmitState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

/**
 * Drives the async submission logic in [CheckoutScreen].
 *
 * The UI is responsible for building [PaymentSummary] from the selected method;
 * this ViewModel owns the network call and the resulting [SubmitState].
 */
class CheckoutViewModel : ViewModel() {

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    /**
     * Sends the order to the backend, then populates [OrderSession] with the
     * real backend IDs on success.
     *
     * @param clienteId  Backend ID of the client placing the order.
     * @param cartItems  Snapshot of the cart from [OrderSession].
     * @param paymentSummary Already-built summary; stored in session on success.
     */
    fun submitOrder(
        clienteId: Long,
        cartItems: List<CartItem>,
        paymentSummary: PaymentSummary
    ) {
        if (_submitState.value is SubmitState.Submitting) return  // guard double-tap

        viewModelScope.launch {
            _submitState.value = SubmitState.Submitting
            try {
                val response = PedidoRepository.createPedido(clienteId, cartItems, paymentSummary)

                // Persist order data in the session.
                OrderSession.paymentSummary = paymentSummary
                OrderSession.pedidoId       = response.id
                OrderSession.pedidoNumero   = response.numero
                OrderSession.invoiceRef     = "PED-%05d".format(response.numero)

                // Track payment completeness so OrdersScreen can enforce the
                // "only facturar when fully paid" business rule without a
                // dedicated backend field (TODO: remove when API exposes it).
                OrderSession.pagoCompleto[response.id] =
                    (paymentSummary.pendingAmount == 0.0)

                _submitState.value = SubmitState.Success
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("Unable to resolve host") == true ->
                        "No se puede conectar al servidor.\nVerifica la URL en RetrofitClient."
                    e.message?.contains("timeout") == true ->
                        "La petición tardó demasiado. Inténtalo de nuevo."
                    else ->
                        e.message ?: "Error desconocido al crear el pedido."
                }
                _submitState.value = SubmitState.Error(message)
            }
        }
    }

    /** Called after [SubmitState.Success] has been handled (navigation triggered). */
    fun resetState() {
        _submitState.value = SubmitState.Idle
    }

    /** Called when the user dismisses the error banner. */
    fun dismissError() {
        if (_submitState.value is SubmitState.Error) {
            _submitState.value = SubmitState.Idle
        }
    }
}
