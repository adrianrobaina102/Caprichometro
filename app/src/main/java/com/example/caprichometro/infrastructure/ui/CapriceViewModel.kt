package com.example.caprichometro.infrastructure.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caprichometro.application.usecase.SaveCapriceUseCase
import com.example.caprichometro.domain.model.Caprice
import kotlinx.coroutines.launch

class CapriceViewModel(
    private val saveCapriceUseCase: SaveCapriceUseCase
) : ViewModel() {

    fun saveCaprice(name: String, price: Double) {
        viewModelScope.launch {
            val testCaprice = Caprice(
                name = name,
                price = price,
                isCaprice = false,
                timestamp = 10
            )
            saveCapriceUseCase.execute(testCaprice)
        }
    }
}