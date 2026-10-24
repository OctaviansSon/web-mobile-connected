package com.example.taskboard.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskboard.UserDto
import com.example.taskboard.data.AppResult
import com.example.taskboard.data.Repository
import com.example.taskboard.data.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val baseUrl: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val loggedIn: Boolean = false,
    val username: String = "",
    val email: String = ""
)

class AuthViewModel(
    private val repo: Repository,
    private val session: SessionStore
) : ViewModel() {

    private val _state = MutableStateFlow(
        AuthUiState(
            baseUrl = session.baseUrl,
            loggedIn = session.token != null,
            username = session.username ?: "",
            email = session.email ?: ""
        )
    )
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        // a 401 anywhere in the app ends the session
        viewModelScope.launch {
            repo.sessionExpired.collect {
                _state.update { it.copy(loggedIn = false, error = "Session expired, please sign in again") }
            }
        }
    }

    fun onBaseUrlChange(url: String) = _state.update { it.copy(baseUrl = url) }

    fun clearError() = _state.update { it.copy(error = null) }

    fun login(email: String, password: String) = submit { repo.login(_state.value.baseUrl, email, password) }

    fun register(username: String, email: String, password: String) =
        submit { repo.register(_state.value.baseUrl, username, email, password) }

    private fun submit(block: suspend () -> AppResult<UserDto>) {
        if (_state.value.loading) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = block()) {
                is AppResult.Ok -> _state.update {
                    it.copy(loading = false, loggedIn = true, username = result.value.username, email = result.value.email)
                }
                is AppResult.Err -> _state.update { it.copy(loading = false, error = result.message) }
            }
        }
    }

    fun logout() {
        session.clear()
        _state.update { it.copy(loggedIn = false, username = "", email = "") }
    }
}
