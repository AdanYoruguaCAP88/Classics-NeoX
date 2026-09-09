package com.classicsneox.solitaire.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classicsneox.solitaire.domain.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SolitaireViewModel : ViewModel() {
    private val engine = GameEngine()
    private val _state = MutableStateFlow(engine.newGame())
    val state: StateFlow<GameState> = _state.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    private var selected: Source? = null
    private var timerJob: Job? = null

    init { startTimer() }

    fun select(source: Source) {
        val current = selected
        if (current == null) {
            selected = source
            _message.value = null
            return
        }
        val destination = when (source) {
            is Source.Tableau -> Destination.Tableau(source.column)
            Source.Waste -> null
            is Source.Foundation -> Destination.Foundation(source.index)
        }
        if (destination != null) {
            val result = engine.move(_state.value, current, destination)
            if (result.accepted) {
                _state.value = result.state
                _message.value = null
            } else _message.value = result.reason
        }
        selected = null
    }

    fun moveToTableau(column: Int) {
        val current = selected ?: return
        val result = engine.move(_state.value, current, Destination.Tableau(column))
        if (result.accepted) { _state.value = result.state; _message.value = null }
        else _message.value = result.reason
        selected = null
    }

    fun moveToFoundation(index: Int) {
        val current = selected ?: return
        val result = engine.move(_state.value, current, Destination.Foundation(index))
        if (result.accepted) { _state.value = result.state; _message.value = null }
        else _message.value = result.reason
        selected = null
    }

    fun draw() { _state.value = engine.draw(_state.value); selected = null }
    fun undo() { _state.value = engine.undo(_state.value); selected = null }
    fun newGame() { _state.value = engine.newGame(); selected = null; _message.value = null }
    fun dismissMessage() { _message.value = null }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) { delay(1000); if (!_state.value.isComplete) _state.value = engine.tick(_state.value) }
        }
    }

    override fun onCleared() { timerJob?.cancel(); super.onCleared() }
}
