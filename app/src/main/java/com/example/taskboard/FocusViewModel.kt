package com.example.taskboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FocusState(
    val sessionMinutes: Int = 25,
    val remainingSeconds: Int = 25 * 60,
    val running: Boolean = false,
    val completedSessions: Int = 0,
    val topic: Category = Category.KUBERNETES,
    val minutesByTopic: Map<Category, Int> = emptyMap()
) {
    val totalFocusMinutes: Int get() = minutesByTopic.values.sum()
    val elapsedFraction: Float get() = 1f - remainingSeconds.toFloat() / (sessionMinutes * 60)
}

/** Study timer. The countdown is a coroutine in viewModelScope, so it survives rotation. */
class FocusViewModel : ViewModel() {

    private val _state = MutableStateFlow(FocusState())
    val state: StateFlow<FocusState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun toggle() = if (_state.value.running) pause() else start()

    private fun start() {
        if (timerJob?.isActive == true) return
        _state.update { it.copy(running = true) }
        timerJob = viewModelScope.launch {
            while (_state.value.remainingSeconds > 0) {
                delay(1000)
                _state.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
            _state.update {
                val spent = (it.minutesByTopic[it.topic] ?: 0) + it.sessionMinutes
                it.copy(
                    running = false,
                    completedSessions = it.completedSessions + 1,
                    remainingSeconds = it.sessionMinutes * 60,
                    minutesByTopic = it.minutesByTopic + (it.topic to spent)
                )
            }
        }
    }

    fun pause() {
        timerJob?.cancel()
        _state.update { it.copy(running = false) }
    }

    fun reset() {
        timerJob?.cancel()
        _state.update { it.copy(running = false, remainingSeconds = it.sessionMinutes * 60) }
    }

    fun setSessionMinutes(minutes: Int) {
        if (_state.value.running) return
        _state.update { it.copy(sessionMinutes = minutes, remainingSeconds = minutes * 60) }
    }

    fun setTopic(topic: Category) {
        if (_state.value.running) return
        _state.update { it.copy(topic = topic) }
    }
}
