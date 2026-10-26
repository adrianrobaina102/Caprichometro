package com.example.caprichometro.domain.model

data class UserProfile(
    val uid: String,
    val username: String,
    val createdAt: Long = System.currentTimeMillis()
)
