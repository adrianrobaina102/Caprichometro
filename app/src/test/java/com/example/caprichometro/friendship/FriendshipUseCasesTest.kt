package com.example.caprichometro.friendship

import com.example.caprichometro.application.usecase.friendship.GetAcceptedFriendsUseCase
import com.example.caprichometro.application.usecase.friendship.GetIncomingFriendRequestsUseCase
import com.example.caprichometro.application.usecase.friendship.RespondToFriendRequestUseCase
import com.example.caprichometro.domain.friendship.Friendship
import com.example.caprichometro.domain.friendship.FriendshipRepository
import com.example.caprichometro.domain.friendship.FriendshipStatus
import com.example.caprichometro.domain.model.UserProfile
import com.example.caprichometro.domain.model.UserProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date

class FriendshipUseCasesTest {

    private val friendshipRepository: FriendshipRepository = mockk()
    private val userProfileRepository: UserProfileRepository = mockk()

    @Nested
    inner class IncomingRequestsTests {
        private val useCase = GetIncomingFriendRequestsUseCase(friendshipRepository, userProfileRepository)

        @Test
        fun `filtra y emite solo solicitudes pendientes donde el usuario es el receptor`() = runTest {
            val myId = "user_me"
            val senderId = "user_sender"
            val friendship = Friendship(
                id = "${senderId}_${myId}",
                memberIDs = listOf(senderId, myId),
                requestedBy = senderId,
                status = FriendshipStatus.PENDING,
                createdAt = Date(),
                updatedAt = Date()
            )

            val senderProfile = UserProfile(uid = senderId, username = "amigo_sender")
            coEvery { friendshipRepository.getFriendships(myId) } returns flowOf(listOf(friendship))
            coEvery { userProfileRepository.getUserById(senderId) } returns Result.success(senderProfile)

            val result = useCase.execute(myId).first()
            assertEquals(1, result.size)
            assertEquals("amigo_sender", result[0].sender.username)
        }

        @Test
        fun `no descarta la solicitud si el perfil del remitente no se encuentra en users`() = runTest {
            val myId = "user_me"
            val senderId = "user_sender_unknown"
            val friendship = Friendship(
                id = "${senderId}_${myId}",
                memberIDs = listOf(senderId, myId),
                requestedBy = senderId,
                status = FriendshipStatus.PENDING,
                createdAt = Date(),
                updatedAt = Date()
            )

            coEvery { friendshipRepository.getFriendships(myId) } returns flowOf(listOf(friendship))
            coEvery { userProfileRepository.getUserById(senderId) } returns Result.success(null)

            val result = useCase.execute(myId).first()
            assertEquals(1, result.size)
            assertEquals("Usuario", result[0].sender.username)
        }
    }

    @Nested
    inner class RespondToRequestTests {
        private val useCase = RespondToFriendRequestUseCase(friendshipRepository)

        @Test
        fun `aceptar solicitud actualiza el estado a ACCEPTED`() = runTest {
            coEvery { friendshipRepository.updateFriendshipStatus("friendship_123", FriendshipStatus.ACCEPTED) } returns Result.success(Unit)

            val result = useCase.execute("friendship_123", accept = true)
            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { friendshipRepository.updateFriendshipStatus("friendship_123", FriendshipStatus.ACCEPTED) }
        }

        @Test
        fun `rechazar solicitud actualiza el estado a DECLINED`() = runTest {
            coEvery { friendshipRepository.updateFriendshipStatus("friendship_123", FriendshipStatus.DECLINED) } returns Result.success(Unit)

            val result = useCase.execute("friendship_123", accept = false)
            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { friendshipRepository.updateFriendshipStatus("friendship_123", FriendshipStatus.DECLINED) }
        }
    }

    @Nested
    inner class AcceptedFriendsTests {
        private val useCase = GetAcceptedFriendsUseCase(friendshipRepository, userProfileRepository)

        @Test
        fun `emite la lista de amigos aceptados con sus perfiles de usuario`() = runTest {
            val myId = "user_me"
            val friendId = "user_friend"
            val friendship = Friendship(
                id = "${myId}_${friendId}",
                memberIDs = listOf(myId, friendId),
                requestedBy = myId,
                status = FriendshipStatus.ACCEPTED,
                createdAt = Date(),
                updatedAt = Date()
            )
            val friendProfile = UserProfile(uid = friendId, username = "buen_amigo")

            coEvery { friendshipRepository.getFriendships(myId) } returns flowOf(listOf(friendship))
            coEvery { userProfileRepository.getUserById(friendId) } returns Result.success(friendProfile)

            val friends = useCase.execute(myId).first()
            assertEquals(1, friends.size)
            assertEquals("buen_amigo", friends[0].username)
        }
    }
}
