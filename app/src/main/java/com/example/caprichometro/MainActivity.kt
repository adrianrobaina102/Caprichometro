package com.example.caprichometro.infrastructure.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.caprichometro.application.usecase.SaveCapriceUseCase
import com.example.caprichometro.application.usecase.auth.RegisterUseCase
import com.example.caprichometro.application.usecase.auth.SignInUseCase
import com.example.caprichometro.infrastructure.auth.FirebaseAuthManager
import com.example.caprichometro.infrastructure.auth.FirebaseAuthRepository
import com.example.caprichometro.infrastructure.data.FirestoreCapriceRepository
import com.example.caprichometro.infrastructure.ui.auth.AuthScreen
import com.example.caprichometro.infrastructure.ui.auth.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAuthManager().signInAnonymouslyIfNeeded()

        //val repository = FirestoreCapriceRepository()
        //val useCase = SaveCapriceUseCase(repository)
        //val viewModel = CapriceViewModel(useCase)
        val authRepository = FirebaseAuthRepository()
        val signInUseCase = SignInUseCase(authRepository)
        val registerUseCase = RegisterUseCase(authRepository)
        val authViewModel = AuthViewModel(signInUseCase, registerUseCase)
        // Pintamos la pantalla
        setContent {
            CapriceApp(authViewModel)
        }
    }
}

@Composable
fun CapriceApp(authViewModel: AuthViewModel) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            //CapriceScreen(viewModel)
            AuthScreen(viewModel = authViewModel)
        }
    }
}
