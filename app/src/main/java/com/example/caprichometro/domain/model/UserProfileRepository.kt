package com.example.caprichometro.domain.model

interface UserProfileRepository {
    suspend fun isUsernameAvailable(username: String): Result<Boolean>
    suspend fun saveProfile(profile: UserProfile): Result<Unit>
    suspend fun getUserById(userId: String): Result<UserProfile?>
    suspend fun deleteProfile(userId: String, username: String): Result<Unit>
}
