package com.classicsneox.wordsearch.api

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.classicsneox.wordsearch.presentation.WordSearchIntent
import com.classicsneox.wordsearch.presentation.WordSearchViewModel
import com.classicsneox.wordsearch.presentation.ui.WordSearchScreen
import kotlinx.coroutines.flow.StateFlow

interface WordSearchGameController {
    val currentScore: StateFlow<Int>
    val isGameOver: StateFlow<Boolean>
    fun restartGame()
    fun pauseGame()
    fun resumeGame()
}

@Composable
fun WordSearchEntryPoint(
    onGameFinished: (finalScore: Int) -> Unit = {}
) {
    val viewModel: WordSearchViewModel = viewModel()
    val state = viewModel.state

    androidx.compose.runtime.LaunchedEffect(state.value.isGameOver) {
        if (state.value.isGameOver) onGameFinished(state.value.score)
    }

    WordSearchScreen(viewModel = viewModel)
}
