package com.classicsneox.solitaire.api

import com.classicsneox.solitaire.domain.GameStats
import com.classicsneox.solitaire.domain.GameState

/** Integration boundary owned by the Solitaire module. */
interface ClassicsGame {
    fun start(): GameState
    fun restart(): GameState
    fun getState(): GameState
    fun getScore(): Int
    fun getStats(): GameStats
}

class SolitarioGame : ClassicsGame {
    private val engine = com.classicsneox.solitaire.domain.GameEngine()
    private var state: GameState = engine.newGame()

    override fun start(): GameState = state
    override fun restart(): GameState { state = engine.newGame(); return state }
    override fun getState(): GameState = state
    override fun getScore(): Int = state.score
    override fun getStats(): GameStats = GameStats(state.score, state.moves, state.elapsedSeconds, if (state.isComplete) 1f else 0f)
}
