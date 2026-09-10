package com.classicsneox.lightsout

import kotlin.random.Random

data class LightsOutState(
    val size: Int = 5,
    val cells: List<Boolean> = emptyList(),
    val moves: Int = 0,
    val seed: Long = 0L,
    val solved: Boolean = false
)

object LightsOutEngine {
    fun newGame(size: Int = 5, seed: Long = Random.nextLong()): LightsOutState {
        require(size in 3..7)

        // Start from a solved board and apply deterministic legal moves.
        // This guarantees that every generated puzzle is solvable.
        val base = List(size * size) { false }
        var state = LightsOutState(size, base, 0, seed, false)
        val random = Random(seed)
        val count = size * size / 2 + 1
        repeat(count) {
            state = press(state, random.nextInt(size * size), countMove = false)
        }

        // Repeated presses can cancel each other out. Avoid presenting an
        // already-solved "new" puzzle; one additional legal press preserves
        // solvability while guaranteeing a playable starting state.
        if (state.solved) {
            state = press(state, 0, countMove = false)
        }

        return state.copy(moves = 0, solved = isSolved(state))
    }

    fun press(state: LightsOutState, index: Int, countMove: Boolean = true): LightsOutState {
        if (index !in state.cells.indices || state.solved) return state
        val row = index / state.size
        val col = index % state.size
        val affected = listOf(
            row to col,
            row - 1 to col,
            row + 1 to col,
            row to col - 1,
            row to col + 1
        )
        val next = state.cells.toMutableList()
        affected.forEach { (r, c) ->
            if (r in 0 until state.size && c in 0 until state.size) {
                val i = r * state.size + c
                next[i] = !next[i]
            }
        }
        return state.copy(
            cells = next,
            moves = if (countMove) state.moves + 1 else state.moves,
            solved = next.none { it }
        )
    }

    fun isSolved(state: LightsOutState): Boolean = state.cells.none { it }

    fun reset(state: LightsOutState): LightsOutState = newGame(state.size)
}
