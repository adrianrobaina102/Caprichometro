package com.example.caprichometro.infrastructure.auth

import com.example.caprichometro.domain.model.AuthRepository
import com.example.caprichometro.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Error al obtener el usuario de Firebase")

            Result.success(mapToDomain(firebaseUser))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Error al crear el usuario en Firebase")


            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            Result.success(mapToDomain(firebaseUser, name))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        // Lo dejamos preparado para cuando implementes el botón de Google
        return Result.failure(NotImplementedError("Google Sign In pendiente de implementar"))
    }
    private fun mapToDomain(
        firebaseUser: com.google.firebase.auth.FirebaseUser,
        overrideName: String? = null
    ): User {
        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            name = overrideName ?: firebaseUser.displayName ?: "Usuario",
            birthday = null // Firebase Auth no guarda fechas. Si necesitas el cumpleaños, lo añadiremos luego con Firebase Firestore.
        )
    }
}