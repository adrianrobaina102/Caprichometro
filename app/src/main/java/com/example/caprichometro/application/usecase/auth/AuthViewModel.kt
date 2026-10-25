package com.example.caprichometro.application.usecase.auth

import com.example.caprichometro.domain.model.AuthRepository
import com.example.caprichometro.domain.model.User

class SignInUseCase(private val authRepository: AuthRepository) {
    suspend fun execute(email: String, password: String): Result<User> {
        // Aquí podrías meter lógica de negocio extra antes de llamar al repo si hiciera falta
        return authRepository.signIn(email, password)
    }
}

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend fun execute(email: String, password: String, name: String): Result<User> {
        return authRepository.register(email, password, name)
    }
}