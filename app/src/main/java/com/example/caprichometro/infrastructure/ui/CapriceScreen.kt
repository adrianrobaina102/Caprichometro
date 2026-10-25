package com.example.caprichometro.infrastructure.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colores del tema
private val DarkBackground = Color(0xFF0F0F0F)
private val DarkSurface = Color(0xFF1C1C1C)
private val LimeGreen = Color(0xFFD4FF00)
private val TextGray = Color(0xFFAAAAAA)

@Composable
fun CapriceScreen(
    viewModel: CapriceViewModel,
    onNavigateToActivePoll: () -> Unit
) {
    // Estados de la UI para capturar lo que escribe el usuario
    var isCompraType by remember { mutableStateOf(true) }
    var questionText by remember { mutableStateOf("") }
    var answer1 by remember { mutableStateOf("") }
    var answer2 by remember { mutableStateOf("") }

    // Validación: El botón solo se activará si hay texto en todas las cajas
    val isFormValid = questionText.isNotBlank() && answer1.isNotBlank() && answer2.isNotBlank()

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.clickable { /* Navegar atrás */ }
                )
                Text(
                    text = "Antes de lanzarlo",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.width(24.dp))
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    // Calculamos el tipo como un String según el botón seleccionado
                    val typeString = if (isCompraType) "Compra" else "Plan"

                    // Llamamos a tu ViewModel actualizado
                    viewModel.saveCaprice(
                        type = typeString,
                        questionText = questionText,
                        answer1 = answer1,
                        answer2 = answer2
                    )

                    viewModel.fetchUserCaprices(limit = 1)

                    onNavigateToActivePoll()
                },
                enabled = isFormValid, // Deshabilita el botón si faltan datos
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LimeGreen,
                    disabledContainerColor = Color(0xFF4A5500) // Un verde apagado si está desactivado
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = if (isFormValid) Color.Black else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Publicar · 5 min",
                    color = if (isFormValid) Color.Black else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Caja de la Foto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF8E8E8E), RoundedCornerShape(12.dp))
                    .clickable { /* Lógica de foto en el futuro */ },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cámara",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Elegir o Tomar Foto", color = Color.White, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Selector de Tipo de Duda
            Text(
                text = "¿QUÉ TIPO DE DUDA?",
                color = TextGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                TypeButton(
                    text = "Compra",
                    isSelected = isCompraType,
                    onClick = { isCompraType = true }
                )
                Spacer(modifier = Modifier.width(12.dp))
                TypeButton(
                    text = "Plan",
                    isSelected = !isCompraType,
                    onClick = { isCompraType = false }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pregunta
            Text(
                text = "Tu pregunta",
                color = TextGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            CustomTextField(
                value = questionText,
                onValueChange = { questionText = it },
                placeholder = "¿Me las pillo por 89 €?"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Respuestas
            Text(
                text = "Dos respuestas. Sin complicarlo.",
                color = TextGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                CustomTextField(
                    value = answer1,
                    onValueChange = { answer1 = it },
                    placeholder = "Sí, de una",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                CustomTextField(
                    value = answer2,
                    onValueChange = { answer2 = it },
                    placeholder = "Hoy no",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Selector de Audiencia (Solo mis amigos)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = TextGray
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Solo mis amigos", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("12 personas · sin ubicación", color = TextGray, fontSize = 12.sp)
                }
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Privado",
                    tint = TextGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Texto Informativo del Tiempo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = LimeGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "5 minutos desde que pulses publicar.",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Botones redondos tipo "píldora" para Compra/Plan
@Composable
fun TypeButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) Color.White else DarkSurface,
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Transparent else Color(0xFF2A2A2A),
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

// Cajas de texto personalizadas
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray, fontWeight = FontWeight.Normal) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkSurface,
            unfocusedContainerColor = DarkSurface,
            focusedBorderColor = LimeGreen,
            unfocusedBorderColor = Color(0xFF2A2A2A),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        modifier = modifier.fillMaxWidth()
    )
}