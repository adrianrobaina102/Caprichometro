package com.example.caprichometro.infrastructure.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun CapriceScreen(viewModel: CapriceViewModel)
{
    var capriceName by remember { mutableStateOf("") }
    var capricePrice by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp)
    )
    {
        OutlinedTextField(
            value = capriceName,
            onValueChange = { newText -> capriceName = newText },
            label = { Text("Enter caprice name") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = capricePrice,
            onValueChange = { newText -> capricePrice = newText },
            label = { Text("Price (€)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(onClick = {
            viewModel.saveCaprice(capriceName, capricePrice.toDoubleOrNull()?: 0.0)
        },
            enabled = capriceName.isNotBlank() && capricePrice.toDoubleOrNull() != null
        ) {
            Text("Guardar Capricho")
        }
    }
}