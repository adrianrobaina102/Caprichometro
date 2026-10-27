package com.example.caprichometro.application.usecase.auth

import com.example.caprichometro.domain.model.AuthRepository
import com.example.caprichometro.domain.model.User
import com.example.caprichometro.domain.model.UserProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RegisterUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var useCase: RegisterUseCase

    @BeforeEach
    fun setUp() {
        // Inicializamos los mocks antes de cada test
        authRepository = mockk()
        userProfileRepository = mockk()
        useCase = RegisterUseCase(authRepository, userProfileRepository)
    }

    @Test
    fun `falla instantaneamente si el nombre de usuario es invalido`() = runTest {
        // Ejecutamos con un usuario de 2 letras (inválido)
        val result = useCase.execute("test@mail.com", "123456", "ad")

        // Verificaciones
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidUsernameException)

        // Verificamos que NUNCA llamó a la base de datos
        coVerify(exactly = 0) { userProfileRepository.isUsernameAvailable(any()) }
    }

    @Test
    fun `falla si el nombre de usuario ya esta registrado`() = runTest {
        // Simulamos que la base de datos dice que NO está libre
        coEvery { userProfileRepository.isUsernameAvailable("adrian_dev") } returns Result.success(false)

        val result = useCase.execute("test@mail.com", "123456", "adrian_dev")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UsernameTakenException)

        // Verificamos que no llegó a intentar registrar en Auth
        coVerify(exactly = 0) { authRepository.register(any(), any(), any()) }
    }

    @Test
    fun `falla si Firebase Auth da error`() = runTest {
        coEvery { userProfileRepository.isUsernameAvailable("adrian_dev") } returns Result.success(true)
        coEvery { authRepository.register(any(), any(), any()) } returns Result.failure(Exception("Auth Error"))

        val result = useCase.execute("test@mail.com", "123456", "adrian_dev")

        assertTrue(result.isFailure)
        assertEquals("Auth Error", result.exceptionOrNull()?.message)

        // Verificamos que no intentó guardar el perfil en Firestore tras fallar Auth
        coVerify(exactly = 0) { userProfileRepository.saveProfile(any()) }
    }

    @Test
    fun `exito completo guarda el perfil y devuelve el usuario`() = runTest {
        // Simulamos un camino feliz perfecto
        val mockUser = User(id = "uid123", email = "test@mail.com", name = "adrian_dev")

        coEvery { userProfileRepository.isUsernameAvailable("adrian_dev") } returns Result.success(true)
        coEvery { authRepository.register(any(), any(), any()) } returns Result.success(mockUser)
        coEvery { userProfileRepository.saveProfile(any()) } returns Result.success(Unit)

        val result = useCase.execute("test@mail.com", "123456", "adrian_dev")

        assertTrue(result.isSuccess)
        assertEquals(mockUser, result.getOrNull())

        // Verificamos que llamó al batch de Firestore correctamente
        coVerify(exactly = 1) { userProfileRepository.saveProfile(match { it.uid == "uid123" && it.username == "adrian_dev" }) }
    }
}