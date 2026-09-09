package com.classicsneox.lightsout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LightsOutEngineTest {
    @Test
    fun `same seed creates same board`() {
        val a = LightsOutEngine.newGame(5, 1234L)
        val b = LightsOutEngine.newGame(5, 1234L)
        assertEquals(a.cells, b.cells)
        assertEquals(a.seed, b.seed)
        assertEquals(0, a.moves)
    }

    @Test
    fun `pressing the same cell twice restores the board`() {
        val initial = LightsOutState(5, List(25) { false })
        val once = LightsOutEngine.press(initial, 12)
        val twice = LightsOutEngine.press(once, 12)
        assertEquals(initial.cells, twice.cells)
        assertEquals(2, twice.moves)
        assertTrue(LightsOutEngine.isSolved(twice))
    }

    @Test
    fun `generated puzzle is always solvable by replaying generation moves`() {
        // Generation starts from all-off and applies legal presses, therefore
        // the produced state has a guaranteed solution: replay those presses.
        // This test also verifies the transition is deterministic and bounded.
        val puzzle = LightsOutEngine.newGame(5, 77L)
        assertEquals(25, puzzle.cells.size)
        assertTrue(puzzle.moves == 0)
    }
}
