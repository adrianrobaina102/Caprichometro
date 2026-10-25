package com.example.caprichometro.application

import com.example.caprichometro.application.usecase.auth.RegisterUseCase
import com.example.caprichometro.domain.model.AuthRepository
import com.example.caprichometro.domain.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RegisterUseCaseTest {

    private val authRepository = mockk<AuthRepository>()
    private val registerUseCase = RegisterUseCase(authRepository)

    @Test
    fun `debe registrar un usuario exitosamente`() = runTest {
        // Arrange
        val email = "adrian@caprichometro.com"
        val password = "securepassword"
        val name = "Adrian"
        val expectedUser = User(id = "2", email = email, name = name)

        coEvery { authRepository.register(email, password, name) } returns Result.success(expectedUser)

        // Act
        val result = registerUseCase.execute(email, password, name)

        // Assert
        assertThat(result.isSuccess).isTrue
        assertThat(result.getOrNull()?.name).isEqualTo(name)

        coVerify(exactly = 1) { authRepository.register(email, password, name) }
    }
}