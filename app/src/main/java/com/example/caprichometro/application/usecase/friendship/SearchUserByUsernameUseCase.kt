package com.example.caprichometro.application.usecase.friendship

import com.example.caprichometro.domain.friendship.FriendshipRepository
import com.example.caprichometro.domain.model.UserProfile

class SearchUserByUsernameUseCase (
    private val friendshipRepository: FriendshipRepository
)
{
     suspend fun execute(username: String): Result<UserProfile?> {
        val cleanUsername = username.trim().removePrefix("@");
         if (cleanUsername.isBlank()) {
             return Result.failure(Exception("El nombre de usuario no puede estar vacío"))
         }
         return friendshipRepository.searchUserByUsername(cleanUsername)
    }

}