package com.classicsneox.solitaire.domain

import kotlin.random.Random

class DeckGenerator {
    fun createShuffled(seed: Long? = null): List<Card> {
        var id = 0
        val deck = Suit.entries.flatMap { suit -> Rank.entries.map { rank -> Card(id++, suit, rank) } }
        return if (seed == null) deck.shuffled() else deck.shuffled(Random(seed))
    }
}

class GameEngine(private val deckGenerator: DeckGenerator = DeckGenerator()) {
    private val history = ArrayDeque<GameState>()

    fun newGame(seed: Long? = null): GameState {
        val deck = deckGenerator.createShuffled(seed).toMutableList()
        val columns = MutableList(7) { mutableListOf<Card>() }

        // Deal Klondike's 28-card tableau. The top card of every tableau
        // column is face-up; all cards underneath remain face-down.
        for (column in 0 until 7) {
            repeat(column + 1) { index ->
                columns[column].add(deck.removeAt(0).copy(faceUp = index == column))
            }
        }

        history.clear()
        return GameState(columns, List(4) { emptyList() }, deck, emptyList())
    }

    fun move(state: GameState, source: Source, destination: Destination): MoveResult {
        val extracted = extract(state, source) ?: return MoveResult(state, false, "Movimiento inválido")
        val cards = extracted.second
        if (cards.isEmpty()) return MoveResult(state, false, "No hay cartas para mover")
        if (!canPlace(cards, destination, extracted.first)) return MoveResult(state, false, "Movimiento no permitido")
        val next = place(extracted.first, cards, destination)
        history.addLast(state)
        val scoreDelta = if (destination is Destination.Foundation) 10 else if (source is Source.Tableau && cards.first().faceUp) 5 else 0
        val result = next.copy(score = next.score + scoreDelta, moves = next.moves + 1, isComplete = next.foundations.all { it.size == 13 })
        return MoveResult(result, true)
    }

    fun draw(state: GameState): GameState {
        if (state.stock.isNotEmpty()) {
            val card = state.stock.last().copy(faceUp = true)
            return state.copy(stock = state.stock.dropLast(1), waste = state.waste + card, moves = state.moves + 1)
        }
        if (state.waste.isEmpty()) return state
        return state.copy(stock = state.waste.asReversed().map { it.copy(faceUp = false) }, waste = emptyList(), moves = state.moves + 1)
    }

    fun undo(state: GameState): GameState = if (history.isEmpty()) state else history.removeLast()

    fun tick(state: GameState): GameState = state.copy(elapsedSeconds = state.elapsedSeconds + 1)

    private fun extract(state: GameState, source: Source): Pair<GameState, List<Card>>? = when (source) {
        Source.Waste -> if (state.waste.isEmpty()) null else state.copy(waste = state.waste.dropLast(1)) to listOf(state.waste.last())
        is Source.Foundation -> if (state.foundations[source.index].isEmpty()) null else state.copy(foundations = state.foundations.mapIndexed { i, p -> if (i == source.index) p.dropLast(1) else p }) to listOf(state.foundations[source.index].last())
        is Source.Tableau -> {
            val column = state.tableau.getOrNull(source.column) ?: return null
            if (source.index !in column.indices || !column[source.index].faceUp) return null
            val moving = column.drop(source.index)
            if (!isValidSequence(moving)) return null
            val trimmed = column.take(source.index)
            val flipped = if (trimmed.isNotEmpty() && !trimmed.last().faceUp) trimmed.dropLast(1) + trimmed.last().copy(faceUp = true) else trimmed
            state.copy(tableau = state.tableau.mapIndexed { i, c -> if (i == source.column) flipped else c }) to moving
        }
    }

    private fun canPlace(cards: List<Card>, destination: Destination, state: GameState): Boolean = when (destination) {
        is Destination.Tableau -> {
            val target = state.tableau.getOrNull(destination.column) ?: return false
            if (target.isEmpty()) cards.first().rank == Rank.KING
            else {
                val top = target.last()
                top.faceUp && top.isRed != cards.first().isRed && top.rank.value == cards.first().rank.value + 1
            }
        }
        is Destination.Foundation -> {
            if (cards.size != 1) return false
            val foundation = state.foundations.getOrNull(destination.index) ?: return false
            val card = cards.first()
            if (foundation.isEmpty()) card.rank == Rank.ACE else foundation.last().suit == card.suit && foundation.last().rank.value + 1 == card.rank.value
        }
    }

    private fun place(state: GameState, cards: List<Card>, destination: Destination): GameState = when (destination) {
        is Destination.Tableau -> state.copy(tableau = state.tableau.mapIndexed { i, c -> if (i == destination.column) c + cards else c })
        is Destination.Foundation -> state.copy(foundations = state.foundations.mapIndexed { i, f -> if (i == destination.index) f + cards else f })
    }

    private fun isValidSequence(cards: List<Card>): Boolean = cards.zipWithNext().all { (a, b) -> a.faceUp && b.faceUp && a.isRed != b.isRed && a.rank.value == b.rank.value + 1 }
}
