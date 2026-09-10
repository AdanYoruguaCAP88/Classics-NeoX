package com.classicsneox.solitaire

import com.classicsneox.solitaire.domain.*
import org.junit.Assert.*
import org.junit.Test

class SolitaireEngineTest {
    @Test fun `new game contains all 52 cards and seven tableau columns`() {
        val state = GameEngine().newGame(1234L)
        assertEquals(7, state.tableau.size)
        assertEquals(28, state.tableau.sumOf { it.size })
        assertEquals(24, state.stock.size)
        assertEquals(52, state.tableau.sumOf { it.size } + state.stock.size)
        assertEquals(7, state.tableau.count { it.last().faceUp })
        assertEquals(21, state.tableau.sumOf { column -> column.dropLast(1).count { it.faceUp } })
    }

    @Test fun `ace can move to empty foundation`() {
        val ace = Card(1, Suit.HEARTS, Rank.ACE, true)
        val state = GameState(List(7) { emptyList() }, List(4) { emptyList() }, emptyList(), listOf(ace))
        val result = GameEngine().move(state, Source.Waste, Destination.Foundation(0))
        assertTrue(result.accepted)
        assertEquals(1, result.state.foundations[0].size)
    }

    @Test fun `king can move to empty tableau`() {
        val king = Card(1, Suit.SPADES, Rank.KING, true)
        val state = GameState(listOf(listOf(king), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList()), List(4) { emptyList() }, emptyList(), emptyList())
        val result = GameEngine().move(state, Source.Tableau(0, 0), Destination.Tableau(1))
        assertTrue(result.accepted)
        assertEquals(Rank.KING, result.state.tableau[1].single().rank)
    }
}
