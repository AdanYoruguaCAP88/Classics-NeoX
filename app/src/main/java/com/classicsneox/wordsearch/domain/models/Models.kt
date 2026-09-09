package com.classicsneox.wordsearch.domain.models

data class Point(val x: Int, val y: Int)

data class GridCell(
    val char: Char,
    val position: Point,
    val isPartOfFoundWord: Boolean = false,
    val isCurrentlySelected: Boolean = false
)

data class Word(
    val text: String,
    val isFound: Boolean = false,
    val start: Point? = null,
    val end: Point? = null
)

enum class Direction(val dx: Int, val dy: Int) {
    RIGHT(1, 0), DOWN(0, 1), DOWN_RIGHT(1, 1), UP_RIGHT(1, -1),
    LEFT(-1, 0), UP(0, -1), UP_LEFT(-1, -1), DOWN_LEFT(-1, 1)
}

data class GameState(
    val grid: List<List<GridCell>> = emptyList(),
    val words: List<Word> = emptyList(),
    val isGameOver: Boolean = false,
    val score: Int = 0,
    val selectionStart: Point? = null,
    val currentSelectionPath: List<Point> = emptyList()
)
