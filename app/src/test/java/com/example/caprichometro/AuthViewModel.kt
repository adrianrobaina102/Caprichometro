package com.example.caprichometro.infrastructure.ui.auth

import android.content.Context
import com.example.caprichometro.application.usecase.auth.RegisterUseCase
import com.example.caprichometro.application.usecase.auth.SignInUseCase
import com.example.caprichometro.application.usecase.auth.SignInWithGoogleUseCase
import com.example.caprichometro.domain.model.User
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    // 1. Preparamos los mocks (dobles de prueba) para aislar la capa externa
    private val signInUseCase: SignInUseCase = mockk()
    private val registerUseCase: RegisterUseCase = mockk()
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase = mockk()
    private val mockContext: Context = mockk() // No necesitamos un contexto real de Android

    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        // Trucamos el hilo principal (Main) para que las corrutinas funcionen en los tests
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(signInUseCase, registerUseCase, signInWithGoogleUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signInWithGoogle actualiza el estado a éxito cuando Google devuelve un usuario valido`() = runTest {
        // GIVEN: Configuramos el mock para que devuelva éxito al ejecutar el caso de uso
        val expectedUser = User(id = "123", email = "test@gmail.com", name = "Test User")
        coEvery { signInWithGoogleUseCase.execute(any()) } returns Result.success(expectedUser)

        // WHEN: El usuario pulsa el botón en la interfaz
        viewModel.signInWithGoogle(mockContext)

        // Adelantamos el tiempo de la corrutina para que termine de ejecutarse
        testScheduler.advanceUntilIdle()

        // THEN: Comprobamos con AssertJ que el estado de la UI es el correcto
        val currentState = viewModel.state.value
        assertThat(currentState.isSubmitting).isFalse()
        assertThat(currentState.isAuthSuccessful).isTrue()
        assertThat(currentState.errorMessage).isNull()
    }

    @Test
    fun `signInWithGoogle muestra un error cuando el usuario cancela la ventana nativa`() = runTest {
        // GIVEN: El usuario cierra la ventana emergente de Google (devuelve un fallo)
        val expectedErrorMessage = "Credencial no válida o cancelada"
        coEvery { signInWithGoogleUseCase.execute(any()) } returns Result.failure(Exception(expectedErrorMessage))

        // WHEN: El usuario pulsa el botón pero luego cancela
        viewModel.signInWithGoogle(mockContext)
        testScheduler.advanceUntilIdle()

        // THEN: Comprobamos con AssertJ que el error se propaga a la UI para mostrar un Toast/Snackbar
        val currentState = viewModel.state.value
        assertThat(currentState.isSubmitting).isFalse()
        assertThat(currentState.isAuthSuccessful).isFalse()
        assertThat(currentState.errorMessage).isEqualTo(expectedErrorMessage)
    }
}