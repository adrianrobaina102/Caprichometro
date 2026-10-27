package com.example.caprichometro

import com.example.caprichometro.application.usecase.friendship.RespondToFriendRequestUseCase
import com.example.caprichometro.domain.friendship.FriendshipRepository
import com.example.caprichometro.domain.friendship.FriendshipStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RespondToFriendRequestUseCaseTest {

    private lateinit var friendshipRepository: FriendshipRepository
    private lateinit var useCase: RespondToFriendRequestUseCase

    @BeforeEach
    fun setUp() {
        friendshipRepository = mockk()
        useCase = RespondToFriendRequestUseCase(friendshipRepository)
    }

    @Test
    fun `al aceptar solicitud actualiza estado a ACCEPTED`() = runTest {
        val friendshipId = "userA_userB"
        coEvery { friendshipRepository.updateFriendshipStatus(friendshipId, FriendshipStatus.ACCEPTED) } returns Result.success(Unit)

        val result = useCase.execute(friendshipId, accept = true)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { friendshipRepository.updateFriendshipStatus(friendshipId, FriendshipStatus.ACCEPTED) }
    }

    @Test
    fun `al rechazar solicitud actualiza estado a DECLINED`() = runTest {
        val friendshipId = "userA_userB"
        coEvery { friendshipRepository.updateFriendshipStatus(friendshipId, FriendshipStatus.DECLINED) } returns Result.success(Unit)

        val result = useCase.execute(friendshipId, accept = false)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { friendshipRepository.updateFriendshipStatus(friendshipId, FriendshipStatus.DECLINED) }
    }
}
