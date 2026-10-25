package com.example.caprichometro.domain.model

interface CapriceRepository {
    // Usamos suspend para que sea asíncrono (hilos)
    suspend fun save(caprice: Caprice): Result<String>

    suspend fun getCaprices(userId: String, limit: Int): Result<List<Caprice>>
}