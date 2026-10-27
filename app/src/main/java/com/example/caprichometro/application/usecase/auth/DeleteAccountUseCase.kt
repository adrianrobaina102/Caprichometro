package com.example.caprichometro.application.usecase.auth

import com.example.caprichometro.domain.model.AuthRepository
import com.example.caprichometro.domain.model.UserProfileRepository

class DeleteAccountUseCase(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository
) {
    suspend fun execute(userId: String, username: String): Result<Unit> {
        return try {
            // 1. Eliminamos el perfil de Firestore (users/{uid} y usernames/{username})
            val profileResult = userProfileRepository.deleteProfile(userId, username)
            if (profileResult.isFailure) {
                return profileResult
            }

            // 2. Eliminamos la cuenta de Firebase Auth
            val authResult = authRepository.deleteAccount()
            if (authResult.isFailure) {
                return authResult
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
