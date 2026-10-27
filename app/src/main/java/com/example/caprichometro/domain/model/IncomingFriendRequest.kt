package com.example.caprichometro.domain.model

import java.util.Date

data class IncomingFriendRequest(
    val friendshipId: String,
    val sender: UserProfile,
    val createdAt: Date
)