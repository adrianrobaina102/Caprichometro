package com.example.caprichometro.infrastructure.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val state by viewModel.state.collectAsState()
    var isLoginMode by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val limeGreen = Color(0xFFCEF242)
    val darkGray = Color(0xFF1C1C1E)
    val lightGrayText = Color(0xFFA0A0A0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
            CircularProgressIndicator(
                progress = 0.3f,
                color = limeGreen,
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 2.dp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Caprichómetro",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tus caprichos, tus dudas y tus planes\nen 5 minutos",
            color = lightGrayText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(darkGray, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            AuthTab(
                text = "Iniciar sesión",
                isSelected = isLoginMode,
                modifier = Modifier.weight(1f)
            ) { isLoginMode = true }

            AuthTab(
                text = "Registrarse",
                isSelected = !isLoginMode,
                modifier = Modifier.weight(1f)
            ) { isLoginMode = false }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!isLoginMode) {
            CustomTextField(
                value = state.name,
                onValueChange = viewModel::updateName,
                label = "Nombre"
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.clickable { showDatePicker = true }) {
                CustomTextField(
                    value = state.birthday?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
                    onValueChange = {},
                    label = "Cumpleaños",
                    isReadOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown, // El icono de la flecha
                                contentDescription = "Abrir calendario",
                                tint = Color.Gray
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        CustomTextField(
            value = state.email,
            onValueChange = viewModel::updateEmail,
            label = "Correo electrónico"
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = state.password,
            onValueChange = viewModel::updatePassword,
            label = "Contraseña",
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordToggle = { passwordVisible = !passwordVisible }
        )

        state.errorMessage?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { if (isLoginMode) viewModel.signIn() else viewModel.register() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = limeGreen),
            shape = RoundedCornerShape(12.dp),
            enabled = !state.isSubmitting
        ) {
            Text(
                text = if (isLoginMode) "Iniciar sesión" else "Crear cuenta",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¿Ya tienes cuenta? Inicia sesión",
            color = Color.White,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "o", color = lightGrayText, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = viewModel::signInWithGoogle,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, darkGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continuar con Google", color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Al continuar aceptas los términos",
            color = lightGrayText,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()

                            viewModel.updateBirthday(localDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar", color = limeGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = darkGray
            )
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = darkGray,
                    titleContentColor = limeGreen,
                    headlineContentColor = Color.White,
                    weekdayContentColor = lightGrayText,
                    dayContentColor = Color.White,
                    todayContentColor = limeGreen,
                    todayDateBorderColor = limeGreen,
                    selectedDayContainerColor = limeGreen,
                    selectedDayContentColor = Color.Black
                )
            )
        }
    }
}

@Composable
fun AuthTab(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFF2C2C2E) else Color.Transparent
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFFA0A0A0),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    isReadOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val limeGreen = Color(0xFFCEF242)
    val darkGray = Color(0xFF1C1C1E)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = isReadOnly,
        placeholder = { Text(label, color = Color.Gray) },
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            if (isPassword) {
                Text(
                    text = if (passwordVisible) "Ocultar" else "Ver",
                    color = Color.Gray,
                    modifier = Modifier
                        .clickable { onPasswordToggle() }
                        .padding(end = 16.dp),
                    fontSize = 14.sp
                )
            }
            else {
                trailingIcon?.invoke()
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = darkGray,
            unfocusedContainerColor = darkGray,
            focusedBorderColor = limeGreen,
            unfocusedBorderColor = darkGray,
            cursorColor = limeGreen
        )
    )
}