package com.example.caprichometro.infrastructure.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caprichometro.application.usecase.auth.RegisterUseCase
import com.example.caprichometro.application.usecase.auth.SignInUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AuthState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val birthday: LocalDate? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isAuthSuccessful: Boolean = false
)

class AuthViewModel(
    private val signInUseCase: SignInUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun updateEmail(email: String) { _state.update { it.copy(email = email) } }
    fun updatePassword(password: String) { _state.update { it.copy(password = password) } }
    fun updateName(name: String) { _state.update { it.copy(name = name) } }
    fun clearError() { _state.update { it.copy(errorMessage = null) } }
    fun updateBirthday(date: LocalDate) { _state.update { it.copy(birthday = date) } }
    fun signIn() {
        val currentState = _state.value
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(errorMessage = "El email y la contraseña son obligatorios") }
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _state.update { it.copy(errorMessage = "Por favor, introduce un correo electrónico válido") }
            return
        }

        _state.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            val result = signInUseCase.execute(currentState.email, currentState.password)
            result.fold(
                onSuccess = { user ->
                    _state.update { it.copy(isSubmitting = false, isAuthSuccessful = true) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.message ?: "Error al iniciar sesión"
                        )
                    }
                }
            )
        }
    }

    fun register() {
        val currentState = _state.value

        if (currentState.name.isBlank() || currentState.email.isBlank() || currentState.password.isBlank() || currentState.birthday == null) {
            _state.update { it.copy(errorMessage = "Todos los campos, incluida la fecha, son obligatorios") }
            return
        }

        if (currentState.birthday.isAfter(java.time.LocalDate.now())) {
            _state.update { it.copy(errorMessage = "La fecha de nacimiento no puede ser en el futuro") }
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _state.update { it.copy(errorMessage = "Por favor, introduce un correo electrónico válido") }
            return
        }

        if (currentState.password.length < 6) {
            _state.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres") }
            return
        }

        _state.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            val result = registerUseCase.execute(
                currentState.email,
                currentState.password,
                currentState.name
            )
            result.fold(
                onSuccess = { user ->
                    _state.update { it.copy(isSubmitting = false, isAuthSuccessful = true) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.message ?: "Error al registrar la cuenta"
                        )
                    }
                }
            )
        }
    }

    fun signInWithGoogle() {
        _state.update { it.copy(errorMessage = "Login con Google en desarrollo") }
    }
}