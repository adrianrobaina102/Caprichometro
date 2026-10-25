package com.example.caprichometro.infrastructure.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caprichometro.application.usecase.GetCapricesUseCase
import com.example.caprichometro.application.usecase.SaveCapriceUseCase
import com.example.caprichometro.domain.model.Caprice
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CapriceState(
    val isLoading: Boolean = false,
    val latestCaprices: List<Caprice> = emptyList(), // Ahora guardamos una lista
    val error: String? = null
)

class CapriceViewModel(
    private val saveCapriceUseCase: SaveCapriceUseCase,
    private val getCapricesUseCase: GetCapricesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CapriceState())
    val state: StateFlow<CapriceState> = _state.asStateFlow()
    // Consigue el ID del usuario logueado. Si es nulo, devuelve texto vacío.
    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Al guardar, ahora pasamos también el currentUserId
    fun saveCaprice(type: String, questionText: String, answer1: String, answer2: String) {
        if (currentUserId.isBlank()) return // Si no hay usuario logueado, cortamos

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val caprice = Caprice(
                userId = currentUserId, // Guardamos a quién pertenece
                type = type,
                question = questionText,
                answer1 = answer1,
                answer2 = answer2,
                timestamp = System.currentTimeMillis()
            )

            saveCapriceUseCase.execute(caprice)

             fetchUserCaprices(limit = 1)
        }
    }

    // El método que pedías: lee y pide la cantidad deseada
    fun fetchUserCaprices(limit: Int = 1) {
        if (currentUserId.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Le pasamos el ID del usuario y la cantidad que queremos leer
            val result = getCapricesUseCase.execute(currentUserId, limit)

            result.fold(
                onSuccess = { capriceList ->
                    _state.update { it.copy(isLoading = false, latestCaprices = capriceList) }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }
}