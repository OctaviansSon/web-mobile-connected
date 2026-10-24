package com.example.taskboard.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskboard.TeamBody
import com.example.taskboard.TeamDto
import com.example.taskboard.data.Repository
import com.example.taskboard.data.onErr
import com.example.taskboard.data.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeamsUiState(
    val teams: List<TeamDto> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val message: String? = null
)

class TeamsViewModel(private val repo: Repository) : ViewModel() {

    private val _state = MutableStateFlow(TeamsUiState())
    val state: StateFlow<TeamsUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = it.teams.isEmpty(), error = null) }
            repo.call { listTeams() }
                .onOk { teams -> _state.update { it.copy(teams = teams, loading = false) } }
                .onErr { err -> _state.update { it.copy(loading = false, error = err.message) } }
        }
    }

    fun create(name: String, description: String, onCreated: (Int) -> Unit) {
        viewModelScope.launch {
            repo.call { newTeam(TeamBody(name.trim(), description.trim())) }
                .onOk { team -> onCreated(team.id) }
                .onErr { err -> _state.update { it.copy(message = err.message) } }
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }
}
