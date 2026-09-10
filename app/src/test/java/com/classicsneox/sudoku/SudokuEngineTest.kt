package com.classicsneox.sudoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuEngineTest {

    private val engine = SudokuEngine()

    @Test
    fun `newGame produces unique solution puzzle`() {
        val state = engine.newGame(Difficulty.MEDIUM, seed = 42L)
        assertEquals(1, SudokuSolver.countSolutions(state.puzzle))
        assertTrue(state.fixed.count { it } in Difficulty.MEDIUM.clues)
    }

    @Test
    fun `place correct number increases score and does not count mistake`() {
        val state = engine.newGame(Difficulty.EASY, seed = 7L)
        val emptyIndex = (0 until 81).first { state.board.get(it) == 0 }
        val correctValue = state.solution.get(emptyIndex)

        val next = engine.place(state.copy(selectedIndex = emptyIndex), correctValue)

        assertEquals(0, next.mistakes)
        assertTrue(next.score > state.score)
        assertEquals(correctValue, next.board.get(emptyIndex))
    }

    @Test
    fun `place wrong number counts mistake`() {
        val state = engine.newGame(Difficulty.EASY, seed = 11L)
        val emptyIndex = (0 until 81).first { state.board.get(it) == 0 }
        val wrongValue = (1..9).first { it != state.solution.get(emptyIndex) }

        val next = engine.place(state.copy(selectedIndex = emptyIndex), wrongValue)

        assertEquals(1, next.mistakes)
        assertTrue(next.score < state.score || next.score == 0)
    }

    @Test
    fun `board remains immutable after place`() {
        val state = engine.newGame(Difficulty.MEDIUM, seed = 99L)
        val emptyIndex = (0 until 81).first { state.board.get(it) == 0 }
        val originalBoard = state.board

        engine.place(state.copy(selectedIndex = emptyIndex), 5)

        assertEquals(0, originalBoard.get(emptyIndex))
    }

    @Test
    fun `completed board is detected`() {
        val state = engine.newGame(Difficulty.EASY, seed = 3L)
        var current = state
        for (i in 0 until 81) {
            if (!current.fixed[i]) {
                current = engine.place(current.copy(selectedIndex = i), current.solution.get(i))
            }
        }
        assertTrue(current.completed)
        assertTrue(RuleValidator.solved(current.board))
    }
}
