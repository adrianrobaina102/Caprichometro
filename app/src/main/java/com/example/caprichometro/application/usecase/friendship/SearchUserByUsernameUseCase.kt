package com.example.caprichometro.application.usecase.friendship

import com.example.caprichometro.domain.friendship.FriendshipRepository
import com.example.caprichometro.domain.model.UserProfile
import com.example.caprichometro.domain.model.UsernameRules

class SearchUserByUsernameUseCase (
    private val friendshipRepository: FriendshipRepository
)
{
     suspend fun execute(username: String): Result<UserProfile?> {
         // <-- CAMBIO CLAVE: Centralizamos la normalización en las reglas de dominio
         val cleanUsername = UsernameRules.normalize(username)
         if (cleanUsername.isBlank()) {
             return Result.failure(Exception("El nombre de usuario no puede estar vacío"))
         }
         return friendshipRepository.searchUserByUsername(cleanUsername)
    }

}
