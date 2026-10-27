package com.example.caprichometro.application

import com.example.caprichometro.application.usecase.SaveCapriceUseCase
import com.example.caprichometro.domain.model.Caprice
import com.example.caprichometro.domain.model.CapriceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SaveCapriceUseCaseTest {

    private val capriceRepository = mockk<CapriceRepository>()
    private val saveCapriceUseCase = SaveCapriceUseCase(capriceRepository)

    @Test
    fun `debe guardar un capricho correctamente y devolver el id`() = runTest {
        // Arrange: Usamos los campos NUEVOS de tu data class
        val caprice = Caprice(
            userId = "user_123",
            type = "Compra",
            question = "¿Me pillo este teclado mecánico?",
            answer1 = "Sí, de una",
            answer2 = "Mejor ahorra"
            // timestamp se genera solo por defecto
        )

        // El mock devuelve Result<String> como espera tu código
        val expectedId = "firebase_doc_id_123"
        coEvery { capriceRepository.save(caprice) } returns Result.success(expectedId)

        // Act
        val result = saveCapriceUseCase.execute(caprice)

        // Assert
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(expectedId)

        coVerify(exactly = 1) { capriceRepository.save(caprice) }
    }
}