package com.example.caprichometro.domain.model

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, name: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
}