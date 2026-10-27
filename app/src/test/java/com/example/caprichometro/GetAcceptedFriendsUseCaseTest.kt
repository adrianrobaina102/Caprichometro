package com.example.caprichometro

import com.example.caprichometro.application.usecase.friendship.GetAcceptedFriendsUseCase
import com.example.caprichometro.domain.friendship.Friendship
import com.example.caprichometro.domain.friendship.FriendshipRepository
import com.example.caprichometro.domain.friendship.FriendshipStatus
import com.example.caprichometro.domain.model.UserProfile
import com.example.caprichometro.domain.model.UserProfileRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Date

class GetAcceptedFriendsUseCaseTest {

    private lateinit var friendshipRepository: FriendshipRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var useCase: GetAcceptedFriendsUseCase

    @BeforeEach
    fun setUp() {
        friendshipRepository = mockk()
        userProfileRepository = mockk()
        useCase = GetAcceptedFriendsUseCase(friendshipRepository, userProfileRepository)
    }

    @Test
    fun `filtra y devuelve solo las amistades aceptadas`() = runTest {
        val currentUserId = "my_uid"
        val friendId = "friend_uid"

        val accepted = Friendship(
            id = "friend_uid_my_uid",
            memberIDs = listOf("friend_uid", "my_uid"),
            requestedBy = "friend_uid",
            status = FriendshipStatus.ACCEPTED,
            createdAt = Date(),
            updatedAt = Date()
        )
        val pending = Friendship(
            id = "other_my_uid",
            memberIDs = listOf("other", "my_uid"),
            requestedBy = "other",
            status = FriendshipStatus.PENDING,
            createdAt = Date(),
            updatedAt = Date()
        )

        coEvery { friendshipRepository.getFriendships(currentUserId) } returns flowOf(listOf(accepted, pending))
        coEvery { userProfileRepository.getUserById(friendId) } returns Result.success(
            UserProfile(uid = friendId, username = "dani")
        )

        val result = useCase.execute(currentUserId).first()

        assertEquals(1, result.size)
        assertEquals("dani", result.first().username)
    }
}
