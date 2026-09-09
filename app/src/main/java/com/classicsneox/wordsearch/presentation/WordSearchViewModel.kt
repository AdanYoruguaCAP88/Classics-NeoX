package com.classicsneox.wordsearch.presentation

import androidx.lifecycle.ViewModel
import com.classicsneox.wordsearch.domain.engine.GridGenerator
import com.classicsneox.wordsearch.domain.models.GameState
import com.classicsneox.wordsearch.domain.models.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

sealed class WordSearchIntent {
    data object StartGame : WordSearchIntent()
    data class StartSelection(val point: Point) : WordSearchIntent()
    data class DragSelection(val point: Point) : WordSearchIntent()
    data object EndSelection : WordSearchIntent()
}

class WordSearchViewModel : ViewModel() {
    private val generator = GridGenerator()
    private val defaultWords = listOf("KOTLIN", "ANDROID", "COMPOSE", "VIEWMODEL", "GRADLE")

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    fun processIntent(intent: WordSearchIntent) {
        when (intent) {
            WordSearchIntent.StartGame -> startGame()
            is WordSearchIntent.StartSelection -> startSelection(intent.point)
            is WordSearchIntent.DragSelection -> dragSelection(intent.point)
            WordSearchIntent.EndSelection -> validateSelection()
        }
    }

    private fun startGame() {
        _state.value = generator.generate(10, defaultWords)
    }

    private fun startSelection(point: Point) {
        if (_state.value.grid.isEmpty() || _state.value.isGameOver) return
        _state.value = _state.value.copy(
            selectionStart = point,
            currentSelectionPath = listOf(point)
        )
        updateGridSelection()
    }

    private fun dragSelection(point: Point) {
        val start = _state.value.selectionStart ?: return
        val path = calculatePath(start, point)
        _state.value = _state.value.copy(currentSelectionPath = path)
        updateGridSelection()
    }

    internal fun calculatePath(start: Point, current: Point): List<Point> {
        val dx = current.x - start.x
        val dy = current.y - start.y
        if (dx != 0 && dy != 0 && abs(dx) != abs(dy)) return listOf(start)

        val steps = maxOf(abs(dx), abs(dy))
        val stepX = dx.sign()
        val stepY = dy.sign()
        return (0..steps).map { Point(start.x + it * stepX, start.y + it * stepY) }
    }

    private fun updateGridSelection() {
        val path = _state.value.currentSelectionPath.toSet()
        val newGrid = _state.value.grid.map { row ->
            row.map { cell -> cell.copy(isCurrentlySelected = cell.position in path) }
        }
        _state.value = _state.value.copy(grid = newGrid)
    }

    private fun validateSelection() {
        val state = _state.value
        val path = state.currentSelectionPath
        if (path.isEmpty() || state.grid.isEmpty()) return

        val selectedWord = path.joinToString("") { state.grid[it.y][it.x].char.toString() }
        val reversedWord = selectedWord.reversed()

        val updatedWords = state.words.map { word ->
            if (!word.isFound && (word.text == selectedWord || word.text == reversedWord)) {
                word.copy(isFound = true)
            } else word
        }

        val found = updatedWords != state.words
        val newGrid = state.grid.map { row ->
            row.map { cell ->
                cell.copy(
                    isCurrentlySelected = false,
                    isPartOfFoundWord = cell.isPartOfFoundWord || (found && cell.position in path)
                )
            }
        }

        _state.value = state.copy(
            words = updatedWords,
            grid = newGrid,
            selectionStart = null,
            currentSelectionPath = emptyList(),
            score = if (found) state.score + 100 else state.score,
            isGameOver = updatedWords.isNotEmpty() && updatedWords.all { it.isFound }
        )
    }

    private fun Int.sign(): Int = when {
        this > 0 -> 1
        this < 0 -> -1
        else -> 0
    }
}
