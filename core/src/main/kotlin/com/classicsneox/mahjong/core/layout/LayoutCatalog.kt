package com.classicsneox.mahjong.core.layout

import com.classicsneox.mahjong.core.model.BoardLayout
import com.classicsneox.mahjong.core.model.Position

/**
 * Catálogo de layouts embebidos para NeoX Mahjong.
 *
 * Decisión de diseño: en vez de transcribir a mano las coordenadas de un
 * layout clásico de "tortuga" (fácil de copiar mal, dado que ninguna
 * fuente confiable estaba disponible para verificar dígito por dígito),
 * se optó por generar los layouts algorítmicamente a partir de rectángulos
 * anidados y centrados. Esto permite verificar en código -y con tests-
 * que cualquier layout embebido cumple SIEMPRE las 144 fichas, cero
 * duplicados y apoyo completo entre capas, en vez de confiar en una
 * transcripción manual. Es una arquitectura de layouts "de datos": agregar
 * un layout nuevo es agregar una función que devuelva una lista de
 * [Position]; el motor de juego no sabe ni le importa cómo se construyó.
 */
object LayoutCatalog {

    /** Genera un rectángulo de fichas de w x h, en la capa z, con origen en unidades finas. */
    private fun rectLayer(widthTiles: Int, heightTiles: Int, z: Int, originX: Int, originY: Int): List<Position> =
        buildList {
            for (row in 0 until heightTiles) {
                for (col in 0 until widthTiles) {
                    add(Position(originX + Position.TILE_SPAN * col, originY + Position.TILE_SPAN * row, z))
                }
            }
        }

    /**
     * "Pirámide NeoX": layout original de 3 capas escalonadas y centradas.
     * Capa 0: 10x8 (80 fichas) · Capa 1: 8x6 (48 fichas), inset de 1 ficha
     * por lado · Capa 2: 4x4 (16 fichas), centrada sobre la capa 1.
     * 80 + 48 + 16 = 144. Toda ficha de una capa superior queda apoyada
     * sobre al menos una de la capa inferior (ver LayoutCatalogTest).
     */
    val PIRAMIDE_NEOX: BoardLayout by lazy {
        val l0 = rectLayer(widthTiles = 10, heightTiles = 8, z = 0, originX = 0, originY = 0)
        val l1 = rectLayer(widthTiles = 8, heightTiles = 6, z = 1, originX = 2, originY = 2)
        val l2 = rectLayer(widthTiles = 4, heightTiles = 4, z = 2, originX = 6, originY = 4)
        BoardLayout(
            id = "piramide_neox",
            displayName = "Pirámide NeoX",
            positions = l0 + l1 + l2,
        )
    }

    /** Layouts disponibles, en el orden en que deberían ofrecerse al jugador. */
    val all: List<BoardLayout> by lazy { listOf(PIRAMIDE_NEOX) }

    fun byId(id: String): BoardLayout? = all.find { it.id == id }
}
