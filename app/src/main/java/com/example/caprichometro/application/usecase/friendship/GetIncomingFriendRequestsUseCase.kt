package com.example.caprichometro.application.usecase.friendship

import com.example.caprichometro.domain.friendship.FriendshipRepository
import com.example.caprichometro.domain.friendship.FriendshipStatus
import com.example.caprichometro.domain.model.IncomingFriendRequest
import com.example.caprichometro.domain.model.UserProfile
import com.example.caprichometro.domain.model.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetIncomingFriendRequestsUseCase(
    private val friendshipRepository: FriendshipRepository,
    private val userProfileRepository: UserProfileRepository
) {
    fun execute(currentUserId: String): Flow<List<IncomingFriendRequest>> {
        return friendshipRepository.getFriendships(currentUserId).map { friendships ->

            // Filtramos: Solo las pendientes y que hayan sido solicitadas por la otra persona
            val pendingRequests = friendships.filter { friendship ->
                friendship.status == FriendshipStatus.PENDING && friendship.requestedBy != currentUserId
            }

            // Buscamos el perfil de cada persona para tener su @username
            // Si el perfil no se encuentra aún en users, creamos un fallback para no perder la solicitud visualmente
            pendingRequests.map { friendship ->
                val profileResult = userProfileRepository.getUserById(friendship.requestedBy)
                val profile = profileResult.getOrNull()

                val senderProfile = profile ?: UserProfile(
                    uid = friendship.requestedBy,
                    username = "Usuario"
                )

                IncomingFriendRequest(
                    friendshipId = friendship.id,
                    sender = senderProfile,
                    createdAt = friendship.createdAt
                )
            }.sortedByDescending { it.createdAt }
        }
    }
}
