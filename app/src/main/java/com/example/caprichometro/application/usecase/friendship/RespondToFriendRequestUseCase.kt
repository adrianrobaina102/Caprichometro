package com.example.caprichometro.application.usecase.friendship

import com.example.caprichometro.domain.friendship.FriendshipRepository
import com.example.caprichometro.domain.friendship.FriendshipStatus

class RespondToFriendRequestUseCase(
    private val friendshipRepository: FriendshipRepository
) {
    suspend fun execute(friendshipId: String, accept: Boolean): Result<Unit> {
        val newStatus = if (accept) FriendshipStatus.ACCEPTED else FriendshipStatus.DECLINED
        return friendshipRepository.updateFriendshipStatus(friendshipId, newStatus)
    }
}
