package com.example.caprichometro.infrastructure.ui.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// 1. EL ESTADO: Agrupa todas las variables que la UI necesita pintar
data class AuthState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val birthday: Long? = null, // Usamos Long para guardar la fecha en milisegundos
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {

    // 2. FLUX DE DATOS: Privado para modificar, público para leer
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    // 3. MÉTODOS DE ACTUALIZACIÓN DE TEXTO (Llamados desde los OutlinedTextField)
    fun updateEmail(email: String) { _state.update { it.copy(email = email) } }
    fun updatePassword(password: String) { _state.update { it.copy(password = password) } }
    fun updateName(name: String) { _state.update { it.copy(name = name) } }
    fun clearError() { _state.update { it.copy(errorMessage = null) } }

    // 4. MÉTODOS DE ACCIÓN (Llamados desde los Botones)
    fun signIn() {
        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        // TODO: Aquí llamaremos al AuthUseCase de tu Dominio (ej. loginUseCase.execute(...))
    }

    fun register() {
        if (!validateRegistration()) return

        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        // TODO: Aquí llamaremos al AuthUseCase de registro
    }

    fun signInWithGoogle() {
        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        // TODO: Lógica de conexión con Firebase/Google
    }

    // 5. LÓGICA DE VALIDACIÓN INTERNA
    private fun validateRegistration(): Boolean {
        val currentState = _state.value
        if (currentState.name.isBlank() || currentState.email.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(errorMessage = "Todos los campos son obligatorios") }
            return false
        }
        return true
    }
}