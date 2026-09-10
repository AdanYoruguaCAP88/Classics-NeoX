package com.classicsneox.mahjong.core.engine

/**
 * Reglas de puntaje de NeoX Mahjong, aisladas en un solo lugar para poder
 * ajustar el balance del juego sin tocar el motor.
 *
 * - Cada match suma puntos base más un bono por racha (matches seguidos
 *   sin pedir pista, remezclar, ni fallar una selección).
 * - Pedir una pista o remezclar corta la racha y tiene un costo fijo en
 *   puntos (nunca deja el puntaje en negativo).
 */
object ScoreRules {
    const val BASE_MATCH_POINTS = 10
    const val STREAK_BONUS_PER_LEVEL = 2
    const val MAX_STREAK_BONUS_LEVELS = 10
    const val HINT_PENALTY = 15
    const val SHUFFLE_PENALTY = 25

    /** Puntos otorgados por un match, dado el nivel de racha ANTES de sumar este match. */
    fun pointsForMatch(streakBeforeThisMatch: Int): Int {
        val bonusLevels = streakBeforeThisMatch.coerceAtMost(MAX_STREAK_BONUS_LEVELS)
        return BASE_MATCH_POINTS + bonusLevels * STREAK_BONUS_PER_LEVEL
    }

    /** Aplica una penalidad al puntaje actual, sin dejarlo nunca por debajo de cero. */
    fun applyPenalty(currentScore: Int, penalty: Int): Int = (currentScore - penalty).coerceAtLeast(0)
}
