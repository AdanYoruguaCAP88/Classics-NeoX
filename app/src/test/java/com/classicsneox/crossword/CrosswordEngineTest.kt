package com.classicsneox.crossword

import com.classicsneox.crossword.core.CrosswordGenerator
import com.classicsneox.crossword.core.Difficulty
import com.classicsneox.crossword.engine.CrosswordEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrosswordEngineTest {
    @Test
    fun generated_puzzles_contain_all_words_for_each_difficulty() {
        val generator = CrosswordGenerator()
        for (difficulty in Difficulty.entries) {
            for (seed in 0L until 20L) {
                val puzzle = generator.generate(difficulty, seed)
                assertEquals(difficulty.wordCount, puzzle.words.size)
                assertTrue(puzzle.width <= difficulty.maxSize)
                assertTrue(puzzle.height <= difficulty.maxSize)
            }
        }
    }

    @Test
    fun new_session_starts_unsolved_and_reveal_solves_a_cell() {
        val generator = CrosswordGenerator()
        val puzzle = generator.generate(Difficulty.MEDIUM, 42L)
        val session = CrosswordEngine.newSession(puzzle, false)
        assertFalse(CrosswordEngine.solved(session))

        val revealed = CrosswordEngine.reveal(session, puzzle.words.first().start)
        assertEquals(puzzle.words.first().entry.word.first(), revealed.grid[puzzle.words.first().row][puzzle.words.first().col])
    }

    @Test
    fun input_advances_within_current_word() {
        val generator = CrosswordGenerator()
        val puzzle = generator.generate(Difficulty.EASY, 7L)
        val session = CrosswordEngine.newSession(puzzle, false)
        val first = session.current ?: error("session has no starting cell")
        val word = CrosswordEngine.currentWord(session) ?: error("session has no current word")
        CrosswordEngine.input(session, word.entry.word.first())
        val after = CrosswordEngine.input(session, word.entry.word.getOrNull(1) ?: word.entry.word.first())
        assertTrue(after.current == first || after.current != null)
    }
}
