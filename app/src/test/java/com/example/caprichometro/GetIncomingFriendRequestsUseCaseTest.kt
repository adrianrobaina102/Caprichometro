package com.example.caprichometro

import com.example.caprichometro.application.usecase.friendship.GetIncomingFriendRequestsUseCase
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Date

class GetIncomingFriendRequestsUseCaseTest {

    private lateinit var friendshipRepository: FriendshipRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var useCase: GetIncomingFriendRequestsUseCase

    @BeforeEach
    fun setUp() {
        friendshipRepository = mockk()
        userProfileRepository = mockk()
        useCase = GetIncomingFriendRequestsUseCase(friendshipRepository, userProfileRepository)
    }

    @Test
    fun `filtra solo solicitudes pendientes dirigidas al usuario actual`() = runTest {
        val currentUserId = "my_uid"
        val senderId = "sender_uid"

        val incomingPending = Friendship(
            id = "sender_uid_my_uid",
            memberIDs = listOf("sender_uid", "my_uid"),
            requestedBy = "sender_uid",
            status = FriendshipStatus.PENDING,
            createdAt = Date(1000)
        )
        val outgoingPending = Friendship(
            id = "my_uid_other_uid",
            memberIDs = listOf("my_uid", "other_uid"),
            requestedBy = "my_uid", // Enviada por mí -> NO debe salir
            status = FriendshipStatus.PENDING,
            createdAt = Date(2000)
        )
        val alreadyAccepted = Friendship(
            id = "sender2_uid_my_uid",
            memberIDs = listOf("sender2_uid", "my_uid"),
            requestedBy = "sender2_uid",
            status = FriendshipStatus.ACCEPTED, // Ya aceptada -> NO debe salir
            createdAt = Date(3000)
        )

        coEvery { friendshipRepository.getFriendships(currentUserId) } returns flowOf(
            listOf(incomingPending, outgoingPending, alreadyAccepted)
        )
        coEvery { userProfileRepository.getUserById(senderId) } returns Result.success(
            UserProfile(uid = senderId, username = "amigo_sender")
        )

        val result = useCase.execute(currentUserId).first()

        assertEquals(1, result.size)
        assertEquals("sender_uid_my_uid", result.first().friendshipId)
        assertEquals("amigo_sender", result.first().sender.username)
    }

    @Test
    fun `no descarta la solicitud si el perfil del remitente no se encuentra en users`() = runTest {
        val currentUserId = "my_uid"
        val senderId = "unknown_sender"

        val incomingPending = Friendship(
            id = "unknown_sender_my_uid",
            memberIDs = listOf("unknown_sender", "my_uid"),
            requestedBy = senderId,
            status = FriendshipStatus.PENDING,
            createdAt = Date()
        )

        coEvery { friendshipRepository.getFriendships(currentUserId) } returns flowOf(listOf(incomingPending))
        coEvery { userProfileRepository.getUserById(senderId) } returns Result.success(null) // Perfil no encontrado

        val result = useCase.execute(currentUserId).first()

        assertEquals(1, result.size)
        assertEquals("unknown_sender_my_uid", result.first().friendshipId)
        assertTrue(result.first().sender.username.isNotBlank())
    }
}
