package com.example.taskboard.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskboard.*
import com.example.taskboard.data.Repository
import com.example.taskboard.data.onErr
import com.example.taskboard.data.onOk
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeamUiState(
    val team: TeamDetailDto? = null,
    val tasks: List<TaskDto> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val query: String = "",
    val statusFilter: TaskStatus? = null,
    val category: Category? = null,
    val stats: StatsDto? = null,
    val statsError: String? = null,
    val message: String? = null,
    val closed: Boolean = false   // team deleted or left -> the screen goes back
)

/** Board, members and statistics of one team. */
class TeamViewModel(
    private val repo: Repository,
    private val teamId: Int,
    private val myUserId: Int
) : ViewModel() {

    private val _state = MutableStateFlow(TeamUiState())
    val state: StateFlow<TeamUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = it.team == null, error = null) }
            repo.call { teamDetail(teamId) }
                .onOk { team -> _state.update { it.copy(team = team) } }
                .onErr { err -> _state.update { it.copy(error = err.message) } }
            if (_state.value.team != null) fetchTasks()
            _state.update { it.copy(loading = false) }
        }
    }

    private suspend fun fetchTasks() {
        val s = _state.value
        repo.call { listTasks(teamId, s.statusFilter?.apiValue, s.category?.apiValue, s.query.trim().ifEmpty { null }) }
            .onOk { tasks -> _state.update { it.copy(tasks = tasks) } }
            .onErr { err -> _state.update { it.copy(message = err.message) } }
    }

    private suspend fun refreshTeam() {
        repo.call { teamDetail(teamId) }.onOk { team -> _state.update { it.copy(team = team) } }
    }

    // ---- board filters (applied by the API) ----
    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)   // debounce while typing
            fetchTasks()
        }
    }

    fun onStatusFilter(status: TaskStatus?) {
        _state.update { it.copy(statusFilter = status) }
        viewModelScope.launch { fetchTasks() }
    }

    fun onCategoryFilter(category: Category?) {
        _state.update { it.copy(category = category) }
        viewModelScope.launch { fetchTasks() }
    }

    /** Tap on the status pill: Backlog -> In progress -> Done -> Backlog (optimistic update with rollback). */
    fun cycleStatus(task: TaskDto) {
        val previous = task.status
        val next = previous.toStatus().next()
        replaceTask(task.copy(status = next.apiValue))
        viewModelScope.launch {
            repo.call { moveTask(task.id, StatusBody(next.apiValue)) }
                .onOk { saved -> replaceTask(saved) }
                .onErr { err ->
                    replaceTask(task.copy(status = previous))
                    _state.update { it.copy(message = err.message) }
                }
        }
    }

    private fun replaceTask(updated: TaskDto) = _state.update { s ->
        s.copy(tasks = s.tasks.map { if (it.id == updated.id) updated else it })
    }

    // ---- members and team settings ----
    fun addMember(email: String) {
        viewModelScope.launch {
            repo.call { inviteMember(teamId, MemberAddBody(email.trim())) }
                .onOk { member ->
                    refreshTeam()
                    _state.update { it.copy(message = "${member.username} added") }
                }
                .onErr { err -> _state.update { it.copy(message = err.message) } }
        }
    }

    fun removeMember(userId: Int) {
        viewModelScope.launch {
            repo.callEmpty { kickMember(teamId, userId) }
                .onOk {
                    if (userId == myUserId) {
                        _state.update { it.copy(closed = true) }
                    } else {
                        refreshTeam()
                        _state.update { it.copy(message = "Member removed") }
                    }
                }
                .onErr { err -> _state.update { it.copy(message = err.message) } }
        }
    }

    fun saveTeam(name: String, description: String) {
        viewModelScope.launch {
            repo.call { editTeam(teamId, TeamBody(name.trim(), description.trim())) }
                .onOk {
                    refreshTeam()
                    _state.update { it.copy(message = "Team updated") }
                }
                .onErr { err -> _state.update { it.copy(message = err.message) } }
        }
    }

    fun deleteTeam() {
        viewModelScope.launch {
            repo.callEmpty { dropTeam(teamId) }
                .onOk { _state.update { it.copy(closed = true) } }
                .onErr { err -> _state.update { it.copy(message = err.message) } }
        }
    }

    // ---- statistics ----
    fun loadStats() {
        viewModelScope.launch {
            repo.call { teamStats(teamId) }
                .onOk { stats -> _state.update { it.copy(stats = stats, statsError = null) } }
                .onErr { err -> _state.update { it.copy(statsError = err.message) } }
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }
}
