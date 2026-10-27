package com.example.caprichometro.infrastructure.ui.friendship

import com.example.caprichometro.application.usecase.friendship.SearchUserByUsernameUseCase
import com.example.caprichometro.application.usecase.friendship.SendFriendRequestUseCase
import com.example.caprichometro.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caprichometro.application.usecase.friendship.GetIncomingFriendRequestsUseCase
import com.example.caprichometro.domain.model.IncomingFriendRequest
import kotlinx.coroutines.launch

data class FriendshipSearchState(
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val userFound: UserProfile? = null,
    val errorMessage: String? = null,
    val requestSentSuccess: Boolean = false,
    val incomingRequests: List<IncomingFriendRequest> = emptyList()
)

class FriendshipViewModel
    (private val searchUserByUsernameUseCase: SearchUserByUsernameUseCase, private val sendFriendRequestUseCase: SendFriendRequestUseCase, private val getIncomingFriendRequestsUseCase: GetIncomingFriendRequestsUseCase)
    : ViewModel()
{
    private val _state = MutableStateFlow(FriendshipSearchState())
    val state: StateFlow<FriendshipSearchState> = _state.asStateFlow()
    fun listenToIncomingRequests(currentUserId: String) {
        viewModelScope.launch {
            getIncomingFriendRequestsUseCase.execute(currentUserId).collect { requests ->
                _state.update { it.copy(incomingRequests = requests) }
            }
        }
    }
    fun updateSearchQuery(query: String)
    {
        _state.update {it.copy(searchQuery = query, errorMessage = null, userFound = null, requestSentSuccess = false)}
    }
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
    fun searchUser() {
        val query = _state.value.searchQuery
        if (query.isBlank()) return

        _state.update { it.copy(isLoading = true, errorMessage = null, userFound = null) }

        viewModelScope.launch {
            val result = searchUserByUsernameUseCase.execute(query)

            result.fold(
                onSuccess = { profile ->
                    if (profile != null) {
                        _state.update { it.copy(isLoading = false, userFound = profile) }
                    } else {
                        _state.update { it.copy(isLoading = false, errorMessage = "No se ha encontrado ningún usuario con ese nombre.") }
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message ?: "Error al buscar usuario.") }
                }
            )
        }
    }
    fun sendRequest(currentUserId: String) {
        val targetUser = _state.value.userFound ?: return

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = sendFriendRequestUseCase.execute(
                currentUserId = currentUserId,
                targetUserId = targetUser.uid
            )

            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, requestSentSuccess = true, userFound = null, searchQuery = "") }
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message ?: "Error al enviar la solicitud.") }
                }
            )
        }
    }
}