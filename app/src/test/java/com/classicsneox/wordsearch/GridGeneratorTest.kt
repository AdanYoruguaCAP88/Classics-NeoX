package com.classicsneox.wordsearch

import com.classicsneox.wordsearch.domain.engine.GridGenerator
import com.classicsneox.wordsearch.domain.models.Point
import com.classicsneox.wordsearch.presentation.WordSearchViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GridGeneratorTest {
    @Test
    fun generator_places_words_within_grid() {
        val state = GridGenerator(Random(7)).generate(10, listOf("KOTLIN", "JAVA"))
        assertEquals(2, state.words.size)
        state.words.forEach { word ->
            assertNotNull(word.start)
            assertNotNull(word.end)
        }
    }

    @Test
    fun generator_does_not_place_word_longer_than_grid() {
        val state = GridGenerator(Random(7)).generate(5, listOf("TOOLONG"))
        assertTrue(state.words.isEmpty())
    }

    @Test
    fun calculate_path_returns_diagonal_line() {
        val vm = WordSearchViewModel()
        val path = vm.calculatePath(Point(0, 0), Point(3, 3))
        assertEquals(listOf(Point(0, 0), Point(1, 1), Point(2, 2), Point(3, 3)), path)
    }

    @Test
    fun calculate_path_rejects_l_shape() {
        val vm = WordSearchViewModel()
        assertEquals(listOf(Point(0, 0)), vm.calculatePath(Point(0, 0), Point(3, 2)))
    }
}
