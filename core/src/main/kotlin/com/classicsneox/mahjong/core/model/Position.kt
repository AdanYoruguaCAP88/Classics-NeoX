package com.classicsneox.mahjong.core.model

/**
 * Posición de una ficha en el tablero de NeoX Mahjong.
 *
 * El tablero usa una grilla "fina": cada ficha ocupa un footprint de 2x2
 * unidades (x, x+2) x (y, y+2). Esto permite representar el clásico
 * apilado tipo "ladrillo" del Mahjong Solitaire -donde cada ficha de la
 * capa z+1 se apoya centrada sobre el empalme de hasta 4 fichas de la
 * capa z- usando exclusivamente aritmética entera, sin números
 * fraccionarios ni floats.
 *
 * Convención: las capas pares (z=0,2,4...) alinean sus fichas en
 * coordenadas (x,y) pares; las capas impares (z=1,3,5...) las alinean en
 * coordenadas impares (desplazadas +1 en x e y respecto de la capa de
 * abajo). Ese corrimiento de una unidad es lo que genera el solapamiento.
 *
 * @param x coordenada horizontal en la grilla fina.
 * @param y coordenada vertical (profundidad) en la grilla fina.
 * @param z capa/altura. 0 es la capa más baja (piso del tablero).
 */
data class Position(val x: Int, val y: Int, val z: Int) {

    /** Ancho/alto del footprint de cualquier ficha, en unidades de grilla fina. */
    companion object {
        const val TILE_SPAN = 2
    }

    /**
     * Dos posiciones "se solapan" si sus footprints de 2x2 se cruzan en
     * el plano (x, y), sin importar la capa. Se usa tanto para detectar
     * cobertura (capa superior) como para vecinos horizontales (misma capa).
     */
    fun overlapsFootprint(other: Position): Boolean {
        val dx = kotlin.math.abs(this.x - other.x)
        val dy = kotlin.math.abs(this.y - other.y)
        return dx < TILE_SPAN && dy < TILE_SPAN
    }

    /** Posición inmediatamente a la izquierda, en la misma capa. */
    fun leftNeighbor(): Position = copy(x = x - TILE_SPAN)

    /** Posición inmediatamente a la derecha, en la misma capa. */
    fun rightNeighbor(): Position = copy(x = x + TILE_SPAN)
}
