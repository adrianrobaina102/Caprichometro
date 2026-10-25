package com.example.caprichometro.application.usecase.auth

import android.content.Context
import com.example.caprichometro.domain.model.AuthRepository
import com.example.caprichometro.domain.model.User

class SignInWithGoogleUseCase(private val authRepository: AuthRepository) {
    suspend fun execute(context: Context): Result<User> {
        return authRepository.signInWithGoogle(context)
    }
}