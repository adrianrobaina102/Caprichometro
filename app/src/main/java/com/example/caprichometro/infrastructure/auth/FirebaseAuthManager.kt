package com.example.caprichometro.infrastructure.auth

import com.google.firebase.auth.FirebaseAuth


class FirebaseAuthManager {
    fun signInAnonymouslyIfNeeded()
    {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null)
        {
            auth.signInAnonymously()
        }
    }
}