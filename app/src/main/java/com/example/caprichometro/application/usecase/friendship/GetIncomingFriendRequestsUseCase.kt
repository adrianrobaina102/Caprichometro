package com.example.caprichometro.application.usecase.friendship

import com.example.caprichometro.domain.friendship.FriendshipRepository
import com.example.caprichometro.domain.friendship.FriendshipStatus
import com.example.caprichometro.domain.model.IncomingFriendRequest
import com.example.caprichometro.domain.model.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetIncomingFriendRequestsUseCase(
    private val friendshipRepository: FriendshipRepository,
    private val userProfileRepository: UserProfileRepository
) {
    fun execute(currentUserId: String): Flow<List<IncomingFriendRequest>> {
        return friendshipRepository.getFriendships(currentUserId).map { friendships ->

            // Filtramos: Solo las pendientes y que te las hayan enviado a ti
            val pendingRequests = friendships.filter { friendship ->
                friendship.status == FriendshipStatus.PENDING && friendship.requestedBy != currentUserId
            }

            // Buscamos el perfil de cada persona para tener su @username
            pendingRequests.mapNotNull { friendship ->
                val profileResult = userProfileRepository.getUserById(friendship.requestedBy)
                val profile = profileResult.getOrNull()

                if (profile != null) {
                    IncomingFriendRequest(
                        friendshipId = friendship.id,
                        sender = profile,
                        createdAt = friendship.createdAt
                    )
                } else {
                    null
                }
            }.sortedByDescending { it.createdAt }
        }
    }
}