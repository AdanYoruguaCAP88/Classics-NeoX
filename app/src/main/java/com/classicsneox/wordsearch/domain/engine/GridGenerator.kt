package com.classicsneox.wordsearch.domain.engine

import com.classicsneox.wordsearch.domain.models.Direction
import com.classicsneox.wordsearch.domain.models.GameState
import com.classicsneox.wordsearch.domain.models.GridCell
import com.classicsneox.wordsearch.domain.models.Point
import com.classicsneox.wordsearch.domain.models.Word
import kotlin.random.Random

class GridGenerator(private val random: Random = Random.Default) {
    fun generate(size: Int, wordList: List<String>): GameState {
        require(size > 0) { "Grid size must be positive" }

        val grid = Array(size) { Array(size) { ' ' } }
        val placedWords = mutableListOf<Word>()

        wordList.map { it.trim().uppercase() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sortedByDescending { it.length }
            .forEach { wordText ->
                if (wordText.length > size) return@forEach

                val candidates = buildList {
                    repeat(100) {
                        add(
                            Triple(
                                random.nextInt(size),
                                random.nextInt(size),
                                Direction.entries[random.nextInt(Direction.entries.size)]
                            )
                        )
                    }
                }

                val placement = candidates.firstOrNull { (x, y, direction) ->
                    canPlaceWord(grid, wordText, x, y, direction, size)
                }

                if (placement != null) {
                    val (startX, startY, direction) = placement
                    wordText.forEachIndexed { i, char ->
                        grid[startY + i * direction.dy][startX + i * direction.dx] = char
                    }
                    val endX = startX + (wordText.length - 1) * direction.dx
                    val endY = startY + (wordText.length - 1) * direction.dy
                    placedWords += Word(
                        text = wordText,
                        start = Point(startX, startY),
                        end = Point(endX, endY)
                    )
                }
            }

        val finalGrid = grid.mapIndexed { y, row ->
            row.mapIndexed { x, char ->
                GridCell(
                    char = if (char == ' ') ('A'..'Z').random(random) else char,
                    position = Point(x, y)
                )
            }
        }

        return GameState(grid = finalGrid, words = placedWords)
    }

    private fun canPlaceWord(
        grid: Array<Array<Char>>,
        word: String,
        x: Int,
        y: Int,
        direction: Direction,
        size: Int
    ): Boolean {
        val endX = x + (word.length - 1) * direction.dx
        val endY = y + (word.length - 1) * direction.dy
        if (endX !in 0 until size || endY !in 0 until size) return false

        for (i in word.indices) {
            val current = grid[y + i * direction.dy][x + i * direction.dx]
            if (current != ' ' && current != word[i]) return false
        }
        return true
    }
}
