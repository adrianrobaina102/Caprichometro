package com.example.caprichometro.infrastructure.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.caprichometro.application.usecase.GetCapricesUseCase
import com.example.caprichometro.application.usecase.SaveCapriceUseCase
import com.example.caprichometro.application.usecase.auth.RegisterUseCase
import com.example.caprichometro.application.usecase.auth.SignInUseCase
import com.example.caprichometro.application.usecase.auth.SignInWithGoogleUseCase
import com.example.caprichometro.infrastructure.auth.FirebaseAuthRepository
import com.example.caprichometro.infrastructure.data.FirestoreCapriceRepository
import com.example.caprichometro.infrastructure.data.FirestoreUserProfileRepository
import com.example.caprichometro.infrastructure.ui.auth.AuthScreen
import com.example.caprichometro.infrastructure.ui.auth.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val capriceRepository = FirestoreCapriceRepository()
        val saveCapriceUseCase = SaveCapriceUseCase(capriceRepository)
        val getCapricesUseCase = GetCapricesUseCase(capriceRepository)
        val capriceViewModel = CapriceViewModel(saveCapriceUseCase, getCapricesUseCase)

        val userProfileRepository = FirestoreUserProfileRepository()

        val authRepository = FirebaseAuthRepository()
        val signInUseCase = SignInUseCase(authRepository)
        val registerUseCase = RegisterUseCase(authRepository, userProfileRepository)
        val signInWithGoogleUseCase = SignInWithGoogleUseCase(authRepository)
        val authViewModel = AuthViewModel(signInUseCase, registerUseCase, signInWithGoogleUseCase)

        setContent {
            CapriceApp(authViewModel, capriceViewModel)
        }
    }
}

@Composable
fun CapriceApp(authViewModel: AuthViewModel, capriceViewModel: CapriceViewModel) {
    val authState by authViewModel.state.collectAsState()
    val capriceState by capriceViewModel.state.collectAsState()

    var currentScreen by remember { mutableStateOf("create") }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (!authState.isAuthSuccessful) {
                AuthScreen(
                    viewModel = authViewModel,
                    onNavigateToHome = {}
                )
            } else {
                when (currentScreen) {
                    "create" -> {
                        CapriceScreen(
                            viewModel = capriceViewModel,
                            onNavigateToActivePoll = { currentScreen = "active" }
                        )
                    }
                    "active" -> {
                        val latestCaprice = capriceState.latestCaprices.firstOrNull()
                        ActivePollScreen(
                            caprice = latestCaprice,
                            onNavigateBack = { currentScreen = "create" },
                            onTimerFinished = { currentScreen = "result" }
                        )
                    }
                    "result" -> {
                        val latestCaprice = capriceState.latestCaprices.firstOrNull()
                        ResultScreen(
                            caprice = latestCaprice,
                            onGoHome = { currentScreen = "create" }
                        )
                    }
                }
            }
        }
    }
}