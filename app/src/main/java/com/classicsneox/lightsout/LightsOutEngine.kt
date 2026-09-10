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

        val base = List(size * size) { false }
        var state = LightsOutState(size, base, 0, seed, false)
        val random = Random(seed)
        val count = size * size / 2 + 1
        repeat(count) {
            state = press(state, random.nextInt(size * size), countMove = false)
        }

        // The random sequence can theoretically cancel to the solved board.
        // In that case start from the solved board and apply one guaranteed
        // non-empty move. That keeps the puzzle solvable and never pre-solved.
        if (state.solved) {
            val guaranteedIndex = (seed.toInt().and(Int.MAX_VALUE)) % (size * size)
            state = LightsOutState(size, base, 0, seed, false)
            state = press(state, guaranteedIndex, countMove = false)
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
