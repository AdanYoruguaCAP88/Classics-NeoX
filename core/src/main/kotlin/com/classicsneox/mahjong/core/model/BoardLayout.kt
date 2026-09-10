package com.classicsneox.mahjong.core.model

/**
 * Un layout es, ante todo, un dato: una lista de posiciones. No sabe nada
 * de fichas ni de reglas de juego. Esto es deliberado -ver
 * [com.classicsneox.mahjong.core.layout.LayoutCatalog]-: agregar un
 * layout nuevo al juego es agregar una función que devuelva una lista de
 * [Position], nada más. La lógica de "qué está libre" y "cómo se genera
 * un tablero resoluble" vive en el motor y es la misma para cualquier
 * layout que cumpla el contrato de [validate].
 */
data class BoardLayout(
    val id: String,
    val displayName: String,
    val positions: List<Position>,
) {
    /**
     * Resultado de validar un layout contra las reglas estructurales
     * mínimas que el motor necesita para poder operar sobre él.
     */
    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data class Invalid(val reasons: List<String>) : ValidationResult()
    }

    /**
     * Verifica que el layout:
     *  1. tenga exactamente [STANDARD_TILE_COUNT] posiciones (para calzar con el mazo estándar);
     *  2. no tenga posiciones duplicadas (dos fichas no pueden ocupar el mismo lugar);
     *  3. cada ficha en capa z>0 esté "apoyada": exista al menos una ficha en la
     *     capa z-1 cuyo footprint se solape con el de ella (nada de fichas flotando).
     *
     * Se corre como test sobre cada layout embebido (ver LayoutCatalogTest),
     * y queda pública para que capas futuras puedan validar layouts propios
     * antes de sumarlos al catálogo.
     */
    fun validate(): ValidationResult {
        val reasons = mutableListOf<String>()

        if (positions.size != STANDARD_TILE_COUNT) {
            reasons.add("Se esperaban $STANDARD_TILE_COUNT posiciones, hay ${positions.size}")
        }
        if (positions.size != positions.toSet().size) {
            reasons.add("Hay posiciones duplicadas en el layout")
        }
        val byLayer = positions.groupBy { it.z }
        for ((z, tilesAtZ) in byLayer) {
            if (z == 0) continue
            val below = byLayer[z - 1].orEmpty()
            val unsupported = tilesAtZ.filterNot { tile -> below.any { it.overlapsFootprint(tile) } }
            if (unsupported.isNotEmpty()) {
                reasons.add("Capa $z tiene ${unsupported.size} ficha(s) sin apoyo en la capa ${z - 1}")
            }
        }

        return if (reasons.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(reasons)
    }
}
