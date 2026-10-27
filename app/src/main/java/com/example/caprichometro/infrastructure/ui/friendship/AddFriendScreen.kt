package com.example.caprichometro.infrastructure.ui.friendship

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
// Colores de tu app
val DarkBackground = Color(0xFF121212)
val LimeGreen = Color(0xFFD4FF00)
val DarkSurface = Color(0xFF1E1E1E)

@Composable
fun AddFriendScreen(
    viewModel: FriendshipViewModel,
    currentUserId: String,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    // Escuchador de las solicitud en tiempo reaal
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            viewModel.listenToIncomingRequests(currentUserId)
        }
    }
    // Escuchador de errores para mostrar un mensajito flotante
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }
    // Escuchador de éxito al enviar solicitud
    LaunchedEffect(state.requestSentSuccess) {
        if (state.requestSentSuccess) {
            Toast.makeText(context, "¡Solicitud enviada a @${state.userFound?.username ?: ""}!", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Añadir Amigos",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )


        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Escribe un @usuario...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.Gray) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedIndicatorColor = LimeGreen,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. EL BOTÓN DE BÚSQUEDA
        Button(
            onClick = { viewModel.searchUser() },
            enabled = state.searchQuery.isNotBlank() && !state.isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LimeGreen,
                contentColor = Color.Black,
                disabledContainerColor = Color.DarkGray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
            } else {
                Text("Buscar usuario", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 3. LA TARJETA DEL USUARIO ENCONTRADO
        state.userFound?.let { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "@${user.username}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    IconButton(
                        onClick = { viewModel.sendRequest(currentUserId) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Enviar solicitud",
                            tint = LimeGreen
                        )
                    }
                }
            }
        }



    // LISTA DE SOLICITUDES PENDIENTES
    Spacer(modifier = Modifier.height(32.dp))

    if (state.incomingRequests.isNotEmpty()) {
        Text(
            text = "Solicitudes pendientes",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
        )

        state.incomingRequests.forEach { request ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(
                        text = "@${request.sender.username}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Quiere ser tu amigo.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { /* TODO: Aceptar (lo haremos en el siguiente paso) */ },
                            colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Aceptar", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Aceptar")
                        }

                        Button(
                            onClick = { /* TODO: Rechazar (lo haremos en el siguiente paso) */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Rechazar", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Rechazar")
                        }
                    }
                }
            }
        }
    }
    }
}