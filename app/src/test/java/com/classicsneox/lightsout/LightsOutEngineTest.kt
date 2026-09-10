package com.classicsneox.lightsout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LightsOutEngineTest {
    @Test
    fun new_games_are_playable_and_solvable() {
        for (seed in 0L until 50L) {
            val state = LightsOutEngine.newGame(size = 5, seed = seed)
            assertEquals(25, state.cells.size)
            assertFalse("seed=$seed generated an already solved board", state.solved)
        }
    }

    @Test
    fun press_toggles_cell_and_orthogonal_neighbors() {
        val state = LightsOutState(size = 3, cells = List(9) { false })
        val next = LightsOutEngine.press(state, 4)
        assertTrue(next.cells[4])
        assertTrue(next.cells[1])
        assertTrue(next.cells[3])
        assertTrue(next.cells[5])
        assertTrue(next.cells[7])
        assertEquals(1, next.moves)
    }

    @Test
    fun reset_keeps_board_size_and_returns_unsolved_board() {
        val state = LightsOutEngine.newGame(size = 4, seed = 123L)
        val reset = LightsOutEngine.reset(state)
        assertEquals(16, reset.cells.size)
        assertFalse(reset.solved)
        assertEquals(0, reset.moves)
    }
}
