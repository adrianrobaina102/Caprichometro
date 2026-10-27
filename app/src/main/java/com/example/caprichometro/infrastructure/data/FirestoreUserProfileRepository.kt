package com.example.caprichometro.infrastructure.data

import com.example.caprichometro.domain.model.UserProfile
import com.example.caprichometro.domain.model.UserProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirestoreUserProfileRepository (private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) : UserProfileRepository
{
    override suspend fun isUsernameAvailable(username: String): Result<Boolean> {
        return try {
            val document = db.collection("usernames").document().get().await()
            Result.success(!document.exists())
        }catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveProfile(profile: UserProfile): Result<Unit> {
        return try {
            val batch = db.batch()

            val userRef = db.collection("users").document(profile.uid)
            val userData = mapOf(
                "username" to profile.username,
                "createdAt" to profile.createdAt
            )
            batch.set(userRef, userData)

            val usernameRef = db.collection("usernames").document(profile.username)
            val usernameData = mapOf(
                "uid" to profile.uid
            )
            batch.set(usernameRef, usernameData)

            batch.commit().await()
            Result.success(Unit)
        }catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getUserById(userId: String): Result<UserProfile?> {
        return try {
            val userDoc = db.collection("users").document(userId).get().await()
            if (!userDoc.exists()) {
                return Result.success(null)
            }

            val profile = UserProfile(
                uid = userId,
                username = userDoc.getString("username") ?: "",
                createdAt = userDoc.getDate("createdAt") ?: Date()
            )
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}