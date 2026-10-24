package com.example.taskboard.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskboard.MemberDto
import com.example.taskboard.TaskBody
import com.example.taskboard.TaskDto
import com.example.taskboard.data.Repository
import com.example.taskboard.data.onErr
import com.example.taskboard.data.onOk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val NEW_TASK = -1

data class TaskEditUiState(
    val loading: Boolean = true,
    val task: TaskDto? = null,          // null when creating
    val members: List<MemberDto> = emptyList(),
    val saving: Boolean = false,
    val error: String? = null,
    val done: Boolean = false           // saved or deleted -> the screen goes back
)

class TaskEditViewModel(
    private val repo: Repository,
    private val teamId: Int,
    private val taskId: Int
) : ViewModel() {

    private val _state = MutableStateFlow(TaskEditUiState())
    val state: StateFlow<TaskEditUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            var failed = false
            repo.call { teamDetail(teamId) }
                .onOk { team -> _state.update { it.copy(members = team.members) } }
                .onErr { err -> failed = true; _state.update { it.copy(error = err.message) } }
            if (!failed && taskId != NEW_TASK) {
                repo.call { taskById(taskId) }
                    .onOk { task -> _state.update { it.copy(task = task) } }
                    .onErr { err -> _state.update { it.copy(error = err.message) } }
            }
            _state.update { it.copy(loading = false) }
        }
    }

    fun save(body: TaskBody) {
        if (_state.value.saving) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val result = if (taskId == NEW_TASK) repo.call { newTask(teamId, body) } else repo.call { editTask(taskId, body) }
            result
                .onOk { _state.update { it.copy(saving = false, done = true) } }
                .onErr { err -> _state.update { it.copy(saving = false, error = err.message) } }
        }
    }

    fun delete() {
        if (taskId == NEW_TASK || _state.value.saving) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            repo.callEmpty { dropTask(taskId) }
                .onOk { _state.update { it.copy(saving = false, done = true) } }
                .onErr { err -> _state.update { it.copy(saving = false, error = err.message) } }
        }
    }
}
