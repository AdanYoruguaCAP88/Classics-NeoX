package com.classicsneox.solitaire.domain

enum class Suit(val symbol: String, val isRed: Boolean) {
    HEARTS("♥", true), DIAMONDS("♦", true), CLUBS("♣", false), SPADES("♠", false)
}

enum class Rank(val value: Int, val label: String) {
    ACE(1, "A"), TWO(2, "2"), THREE(3, "3"), FOUR(4, "4"), FIVE(5, "5"),
    SIX(6, "6"), SEVEN(7, "7"), EIGHT(8, "8"), NINE(9, "9"), TEN(10, "10"),
    JACK(11, "J"), QUEEN(12, "Q"), KING(13, "K")
}

data class Card(
    val id: Int,
    val suit: Suit,
    val rank: Rank,
    val faceUp: Boolean = false
) {
    val isRed: Boolean get() = suit.isRed
}

data class GameState(
    val tableau: List<List<Card>>,
    val foundations: List<List<Card>>,
    val stock: List<Card>,
    val waste: List<Card>,
    val score: Int = 0,
    val moves: Int = 0,
    val elapsedSeconds: Long = 0,
    val isComplete: Boolean = false
) {
    companion object { fun empty() = GameState(List(7) { emptyList() }, List(4) { emptyList() }, emptyList(), emptyList()) }
}

data class MoveResult(val state: GameState, val accepted: Boolean, val reason: String? = null)

data class GameStats(val score: Int, val moves: Int, val elapsedSeconds: Long, val completionRate: Float)

sealed interface Source {
    data class Tableau(val column: Int, val index: Int) : Source
    data object Waste : Source
    data class Foundation(val index: Int) : Source
}

sealed interface Destination {
    data class Tableau(val column: Int) : Destination
    data class Foundation(val index: Int) : Destination
}
