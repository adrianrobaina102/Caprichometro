package com.example.caprichometro.infrastructure.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.caprichometro.application.usecase.GetCapricesUseCase
import com.example.caprichometro.application.usecase.SaveCapriceUseCase
import com.example.caprichometro.application.usecase.auth.RegisterUseCase
import com.example.caprichometro.application.usecase.auth.SignInUseCase
import com.example.caprichometro.application.usecase.auth.SignInWithGoogleUseCase
import com.example.caprichometro.application.usecase.friendship.GetIncomingFriendRequestsUseCase
import com.example.caprichometro.application.usecase.friendship.SearchUserByUsernameUseCase
import com.example.caprichometro.application.usecase.friendship.SendFriendRequestUseCase
import com.example.caprichometro.infrastructure.auth.FirebaseAuthRepository
import com.example.caprichometro.infrastructure.data.FirestoreCapriceRepository
import com.example.caprichometro.infrastructure.data.FirestoreFriendshipRepository
import com.example.caprichometro.infrastructure.data.FirestoreUserProfileRepository
import com.example.caprichometro.infrastructure.ui.auth.AuthScreen
import com.example.caprichometro.infrastructure.ui.auth.AuthViewModel
import com.example.caprichometro.infrastructure.ui.friendship.AddFriendScreen
import com.example.caprichometro.infrastructure.ui.friendship.FriendshipViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dependencias de Caprichos
        val capriceRepository = FirestoreCapriceRepository()
        val saveCapriceUseCase = SaveCapriceUseCase(capriceRepository)
        val getCapricesUseCase = GetCapricesUseCase(capriceRepository)
        val capriceViewModel = CapriceViewModel(saveCapriceUseCase, getCapricesUseCase)

        // Dependencias de Auth
        val userProfileRepository = FirestoreUserProfileRepository()
        val authRepository = FirebaseAuthRepository()
        val signInUseCase = SignInUseCase(authRepository)
        val registerUseCase = RegisterUseCase(authRepository, userProfileRepository)
        val signInWithGoogleUseCase = SignInWithGoogleUseCase(authRepository)
        val authViewModel = AuthViewModel(signInUseCase, registerUseCase, signInWithGoogleUseCase)

        // Dependencias de Amistad
        val friendshipRepository = FirestoreFriendshipRepository()
        val searchUserUseCase = SearchUserByUsernameUseCase(friendshipRepository)
        val sendRequestUseCase = SendFriendRequestUseCase(friendshipRepository)
        val getIncomingRequestsUseCase = GetIncomingFriendRequestsUseCase(friendshipRepository, userProfileRepository)
        val friendshipViewModel = FriendshipViewModel(searchUserUseCase, sendRequestUseCase, getIncomingRequestsUseCase)

        setContent {
            CapriceApp(authViewModel, capriceViewModel, friendshipViewModel)
        }
    }
}

@Composable
fun CapriceApp(
    authViewModel: AuthViewModel,
    capriceViewModel: CapriceViewModel,
    friendshipViewModel: FriendshipViewModel
) {
    val authState by authViewModel.state.collectAsState()
    val capriceState by capriceViewModel.state.collectAsState()
    val friendshipState by friendshipViewModel.state.collectAsState()

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Escuchamos solicitudes pendientes en tiempo real en cuanto el usuario inicia sesión
    LaunchedEffect(authState.isAuthSuccessful, currentUserId) {
        if (authState.isAuthSuccessful && currentUserId.isNotBlank()) {
            friendshipViewModel.listenToIncomingRequests(currentUserId)
        }
    }

    var currentScreen by remember { mutableStateOf("create") }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (!authState.isAuthSuccessful) {
                AuthScreen(
                    viewModel = authViewModel,
                    onNavigateToHome = {}
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E1E1E))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { currentScreen = "create" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentScreen != "add_friend") Color(0xFFD4FF00) else Color.DarkGray,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Caprichos")
                        }

                        Button(
                            onClick = { currentScreen = "add_friend" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentScreen == "add_friend") Color(0xFFD4FF00) else Color.DarkGray,
                                contentColor = Color.Black
                            )
                        ) {
                            val pendingCount = friendshipState.incomingRequests.size
                            Text(if (pendingCount > 0) "Amigos ($pendingCount)" else "Amigos")
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
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
                            "add_friend" -> {
                                AddFriendScreen(
                                    viewModel = friendshipViewModel,
                                    currentUserId = currentUserId,
                                    onBack = { currentScreen = "create" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
