package com.example.caprichometro.domain.model

data class User(
    val id: String,
    val email: String,
    val name: String,
    val birthday: Long? = null
)