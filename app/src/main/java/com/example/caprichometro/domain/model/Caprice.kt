package com.example.caprichometro.domain.model

data class Caprice(
    val name: String,
    val price: Double,
    val isCaprice: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
