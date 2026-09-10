package com.classicsneox.crossword

import com.classicsneox.crossword.core.*
import com.classicsneox.crossword.engine.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

data class CrosswordUiState(
    val session: CrosswordSession,
    val paused: Boolean,
    val solved: Boolean,
    val score: Long,
    val revealsLeft: Int
)

class CrosswordController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val generator = CrosswordGenerator()
    private var difficulty = Difficulty.MEDIUM
    private var session = CrosswordEngine.newSession(
        generator.generate(difficulty, Random.nextLong()),
        false
    )
    private var paused = false
    private val _state = MutableStateFlow(render())
    val state: StateFlow<CrosswordUiState> = _state.asStateFlow()

    init {
        scope.launch {
            while (isActive) {
                delay(1000)
                if (!paused && !CrosswordEngine.solved(session)) {
                    session = CrosswordEngine.tick(session)
                    emit()
                }
            }
        }
    }

    fun select(p: CellPosition) {
        if (!paused) {
            session = CrosswordEngine.select(session, p)
            emit()
        }
    }

    fun input(c: Char) {
        if (!paused && !CrosswordEngine.solved(session)) {
            session = CrosswordEngine.input(session, c)
            emit()
        }
    }

    fun erase() {
        if (!paused) {
            session = CrosswordEngine.erase(session)
            emit()
        }
    }

    fun toggle() {
        if (!paused) {
            session = CrosswordEngine.toggle(session)
            emit()
        }
    }

    fun check() {
        if (!paused) {
            session = CrosswordEngine.check(session)
            emit()
        }
    }

    fun reveal() {
        if (!paused && state.value.revealsLeft > 0) {
            session = CrosswordEngine.reveal(session)
            emit()
        }
    }

    fun newGame(d: Difficulty = difficulty) {
        difficulty = d
        session = CrosswordEngine.newSession(
            generator.generate(d, Random.nextLong()),
            d.revealFirst
        )
        paused = false
        emit()
    }

    fun pause() {
        paused = true
        emit()
    }

    fun resume() {
        paused = false
        emit()
    }

    fun destroy() {
        scope.cancel()
    }

    private fun score(): Long = (
        difficulty.baseScore -
            session.elapsed * 5L -
            (session.revealed.size - session.initialReveals) * 100L -
            session.checks * 50L
        ).coerceAtLeast(0)

    private fun render() = CrosswordUiState(
        session = session,
        paused = paused,
        solved = CrosswordEngine.solved(session),
        score = score(),
        revealsLeft = (3 - (session.revealed.size - session.initialReveals)).coerceAtLeast(0)
    )

    private fun emit() {
        _state.value = render()
    }
}
