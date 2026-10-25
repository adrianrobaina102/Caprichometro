package com.example.caprichometro.infrastructure.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. DESIGN SYSTEM: Colors extracted cleanly
object AuthStyle {
    val Background = Color(0xFF09090A)
    val Surface = Color(0xFF121212)
    val Border = Color(0xFF292929)
    val Muted = Color(0xFF919191)
    val Lime = Color(0xFFD4FF5C)
}

// 2. MAIN SCREEN: Clean, readable, no logic spaghetti
@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    var isRegistering by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthStyle.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthHeader()
        Spacer(modifier = Modifier.height(26.dp))

        AuthTabs(isRegistering = isRegistering, onTabSelected = { isRegistering = it })
        Spacer(modifier = Modifier.height(24.dp))

        AuthForm(isRegistering = isRegistering, viewModel = viewModel)
        Spacer(modifier = Modifier.height(18.dp))

        AuthToggleText(isRegistering = isRegistering, onToggle = { isRegistering = !isRegistering })
        Spacer(modifier = Modifier.height(24.dp))

        AuthSocialAccess(onGoogleSignIn = { /* viewModel.signInWithGoogle() */ })

        Spacer(modifier = Modifier.weight(1f, fill = false))
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Al continuar aceptas los términos",
            color = AuthStyle.Muted,
            fontSize = 12.sp
        )
    }
}

// 3. COMPONENTS: Extracted to keep functions small
@Composable
private fun AuthHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CaprichometroMark()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Caprichómetro",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Tus caprichos, tus dudas y tus planes\nen 5 minutos",
            color = AuthStyle.Muted,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun CaprichometroMark() {
    // Simulated Canvas Drawing for the Logo
    Canvas(modifier = Modifier.size(112.dp, 96.dp)) {
        drawArc(
            color = Color(0xFF262626),
            startAngle = 166f,
            sweepAngle = 208f,
            useCenter = false,
            style = Stroke(width = 15f, cap = StrokeCap.Round),
            size = Size(size.width, size.height * 0.8f)
        )
        drawArc(
            color = AuthStyle.Lime,
            startAngle = 166f,
            sweepAngle = 94f,
            useCenter = false,
            style = Stroke(width = 15f, cap = StrokeCap.Round),
            size = Size(size.width, size.height * 0.8f)
        )
    }
}

@Composable
private fun AuthTabs(isRegistering: Boolean, onTabSelected: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AuthStyle.Surface, RoundedCornerShape(16.dp))
    ) {
        // Tab: Login
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .background(if (!isRegistering) Color(0xFF1A1A1A) else Color.Transparent, RoundedCornerShape(16.dp))
                .clickable { onTabSelected(false) },
            contentAlignment = Alignment.Center
        ) {
            Text("Iniciar sesión", color = if (!isRegistering) Color.White else AuthStyle.Muted, fontWeight = if (!isRegistering) FontWeight.SemiBold else FontWeight.Normal)
        }
        // Tab: Register
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .background(if (isRegistering) Color(0xFF1A1A1A) else Color.Transparent, RoundedCornerShape(16.dp))
                .clickable { onTabSelected(true) },
            contentAlignment = Alignment.Center
        ) {
            Text("Registrarse", color = if (isRegistering) Color.White else AuthStyle.Muted, fontWeight = if (isRegistering) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}

@Composable
private fun AuthForm(isRegistering: Boolean, viewModel: AuthViewModel) {
    // En Compose, guardamos el estado visual aquí o en el ViewModel.
    // Usaremos variables locales para que la UI no dependa estrictamente del ViewModel aún
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var showsPassword by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isRegistering) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Nombre real", color = AuthStyle.Muted) },
                modifier = Modifier.fillMaxWidth().authFieldStyle(),
                singleLine = true
            )
            // Aquí iría el DatePicker que es un poco más complejo en Compose (lo dejamos como texto por ahora)
            OutlinedTextField(
                value = "",
                onValueChange = { },
                placeholder = { Text("Cumpleaños", color = AuthStyle.Muted) },
                modifier = Modifier.fillMaxWidth().authFieldStyle(),
                enabled = false // Simula el botón de calendario
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Correo electrónico", color = AuthStyle.Muted) },
            modifier = Modifier.fillMaxWidth().authFieldStyle(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Contraseña", color = AuthStyle.Muted) },
            modifier = Modifier.fillMaxWidth().authFieldStyle(),
            visualTransformation = if (showsPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            trailingIcon = {
                Text(
                    text = if (showsPassword) "Ocultar" else "Ver",
                    color = AuthStyle.Muted,
                    modifier = Modifier.clickable { showsPassword = !showsPassword }.padding(end = 16.dp)
                )
            }
        )

        Button(
            onClick = { /* Lógica de login o registro aquí */ },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AuthStyle.Lime),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (isRegistering) "Crear cuenta" else "Entrar",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun AuthToggleText(isRegistering: Boolean, onToggle: () -> Unit) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isRegistering) "¿Ya tienes cuenta? " else "¿No tienes cuenta? ",
            color = Color.White,
            fontSize = 14.sp
        )
        Text(
            text = if (isRegistering) "Inicia sesión" else "Regístrate",
            color = AuthStyle.Lime,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { onToggle() }
        )
    }
}

@Composable
private fun AuthSocialAccess(onGoogleSignIn: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f).height(1.dp).background(AuthStyle.Border))
            Text(" o ", color = AuthStyle.Muted, modifier = Modifier.padding(horizontal = 8.dp))
            Box(modifier = Modifier.weight(1f).height(1.dp).background(AuthStyle.Border))
        }
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = onGoogleSignIn,
            modifier = Modifier.fillMaxWidth().height(52.dp).border(1.dp, AuthStyle.Border, RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = AuthStyle.Surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Continuar con Google", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

// Custom Modifier for text fields to keep them DRY (Don't Repeat Yourself)
fun Modifier.authFieldStyle() = this
    .background(AuthStyle.Surface, RoundedCornerShape(16.dp))
    .border(1.dp, AuthStyle.Border, RoundedCornerShape(16.dp))