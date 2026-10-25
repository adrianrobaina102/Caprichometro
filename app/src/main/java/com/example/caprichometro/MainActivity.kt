package com.example.caprichometro.infrastructure.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.caprichometro.application.usecase.SaveCapriceUseCase
import com.example.caprichometro.application.usecase.auth.RegisterUseCase
import com.example.caprichometro.application.usecase.auth.SignInUseCase
import com.example.caprichometro.application.usecase.auth.SignInWithGoogleUseCase
import com.example.caprichometro.infrastructure.auth.FirebaseAuthManager
import com.example.caprichometro.infrastructure.auth.FirebaseAuthRepository
import com.example.caprichometro.infrastructure.data.FirestoreCapriceRepository
import com.example.caprichometro.infrastructure.ui.auth.AuthScreen
import com.example.caprichometro.infrastructure.ui.auth.AuthViewModel
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Instancias de la pantalla principal (Caprichos)
        val capriceRepository = FirestoreCapriceRepository()
        val saveCapriceUseCase = SaveCapriceUseCase(capriceRepository)
        val capriceViewModel = CapriceViewModel(saveCapriceUseCase)

        // 2. Instancias de la pantalla de Auth
        val authRepository = FirebaseAuthRepository()
        val signInUseCase = SignInUseCase(authRepository)
        val registerUseCase = RegisterUseCase(authRepository)
        val registerGoogleUseCase = SignInWithGoogleUseCase(authRepository);
        val authViewModel = AuthViewModel(signInUseCase, registerUseCase, registerGoogleUseCase)

        setContent {
            CapriceApp(authViewModel, capriceViewModel)
        }
    }
}

@Composable
fun CapriceApp(authViewModel: AuthViewModel, capriceViewModel: CapriceViewModel) {
    // Escuchamos el estado del login
    val authState by authViewModel.state.collectAsState()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (authState.isAuthSuccessful) {
                // Si el login fue bien, cargamos la app real
                CapriceScreen(viewModel = capriceViewModel)
            } else {
                // Si no, lo mantenemos en la puerta de entrada
                AuthScreen(viewModel = authViewModel)
            }
        }
    }
}