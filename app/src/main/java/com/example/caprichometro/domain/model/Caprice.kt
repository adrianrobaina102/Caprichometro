package com.example.caprichometro.domain.model

data class Caprice(
    val userId: String = "",
    val type: String,
    val question: String,
    val answer1: String,
    val answer2: String,
    val timestamp: Long = System.currentTimeMillis()
)
