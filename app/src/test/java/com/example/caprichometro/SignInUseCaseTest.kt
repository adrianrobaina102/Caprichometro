package com.example.caprichometro.application

import com.example.caprichometro.application.usecase.auth.SignInUseCase
import com.example.caprichometro.domain.model.AuthRepository
import com.example.caprichometro.domain.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SignInUseCaseTest {

    // Mockeamos el puerto (la interfaz, no la implementación de Firebase)
    private val authRepository = mockk<AuthRepository>()
    private val signInUseCase = SignInUseCase(authRepository)

    @Test
    fun `debe devolver exito cuando las credenciales son correctas`() = runTest {
        // Arrange (Preparación)
        val email = "test@caprichometro.com"
        val password = "password123"
        val expectedUser = User(id = "1", email = email, name = "Test")

        coEvery { authRepository.signIn(email, password) } returns Result.success(expectedUser)

        // Act (Ejecución)
        val result = signInUseCase.execute(email, password)

        // Assert (Comprobación)
        assertThat(result.isSuccess).isTrue
        assertThat(result.getOrNull()).isEqualTo(expectedUser)

        // Verificamos que el repositorio fue llamado exactamente 1 vez con esos datos
        coVerify(exactly = 1) { authRepository.signIn(email, password) }
    }

    @Test
    fun `debe devolver fallo cuando el repositorio falla`() = runTest {
        // Arrange
        val email = "test@caprichometro.com"
        val password = "wrongpassword"
        val exception = Exception("Credenciales inválidas")

        coEvery { authRepository.signIn(email, password) } returns Result.failure(exception)

        // Act
        val result = signInUseCase.execute(email, password)

        // Assert
        assertThat(result.isFailure).isTrue
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }
}