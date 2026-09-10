package com.classicsneox.mahjong.core

import com.classicsneox.mahjong.core.engine.ScoreRules
import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreRulesTest {

    @Test
    fun `sin racha, un match vale los puntos base`() {
        assertEquals(ScoreRules.BASE_MATCH_POINTS, ScoreRules.pointsForMatch(streakBeforeThisMatch = 0))
    }

    @Test
    fun `la racha suma un bono lineal por nivel`() {
        assertEquals(
            ScoreRules.BASE_MATCH_POINTS + 3 * ScoreRules.STREAK_BONUS_PER_LEVEL,
            ScoreRules.pointsForMatch(streakBeforeThisMatch = 3),
        )
    }

    @Test
    fun `el bono de racha tiene un techo`() {
        val puntosEnElTecho = ScoreRules.pointsForMatch(ScoreRules.MAX_STREAK_BONUS_LEVELS)
        val puntosPasadoElTecho = ScoreRules.pointsForMatch(ScoreRules.MAX_STREAK_BONUS_LEVELS + 50)
        assertEquals(puntosEnElTecho, puntosPasadoElTecho)
    }

    @Test
    fun `una penalidad nunca deja el puntaje negativo`() {
        assertEquals(0, ScoreRules.applyPenalty(currentScore = 5, penalty = ScoreRules.HINT_PENALTY))
    }

    @Test
    fun `una penalidad se resta normalmente cuando alcanza el puntaje`() {
        assertEquals(35, ScoreRules.applyPenalty(currentScore = 50, penalty = ScoreRules.HINT_PENALTY))
    }
}
