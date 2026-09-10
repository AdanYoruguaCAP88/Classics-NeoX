package com.classicsneox.mahjong.core.engine

import com.classicsneox.mahjong.core.model.Position
import com.classicsneox.mahjong.core.model.Tile
import com.classicsneox.mahjong.core.model.TileId
import com.classicsneox.mahjong.core.model.TileType
import kotlin.random.Random

/**
 * Genera repartos de fichas garantizados como resolubles.
 *
 * ## La idea central
 * En vez de tirar fichas al azar y esperar que el tablero se pueda
 * limpiar, se construye la partida "al revés": se calcula un orden de
 * retiro válido ANTES de decidir qué ficha va en cada lugar, y recién
 * después se asignan los valores de manera que ese orden funcione.
 *
 * ## Por qué NO alcanza con "elegir 2 libres cualquiera, repetir"
 * La primera versión de este generador hacía exactamente eso -en cada
 * paso, tomar 2 posiciones libres cualesquiera del conjunto que todavía
 * queda- razonando que como [BoardTopology.isFree] sólo mira qué sigue
 * ocupado, el estado en el paso i coincide con el tablero real tras (i-1)
 * jugadas. Esa parte del razonamiento es correcta, pero no alcanza: un
 * test la hizo caer (ver BoardGeneratorTest/ShuffleTest, capturado antes
 * de este arreglo) porque elegir *cuáles* 2 libres sacar en cada paso, sin
 * mirar hacia adelante, puede llevar a un callejón sin salida -dejar una
 * ficha tapada "huérfana", sin ninguna otra libre con la cual emparejarla-
 * aunque el layout sí tenga alguna secuencia completa posible. Greedy sin
 * backtracking no alcanza para esta estructura.
 *
 * ## El algoritmo correcto: pelar filas, de la capa de arriba hacia abajo
 * En vez de decidir "al azar" qué sacar, se aprovecha la estructura del
 * tablero:
 *  1. Se procesan las capas de la más alta a la más baja. Al llegar a una
 *     capa, TODO lo que había arriba ya fue puesto en el orden (así que,
 *     en el juego real, ya habría sido retirado): nada de esta capa está
 *     tapado.
 *  2. Dentro de una capa, se agrupan las posiciones por fila (mismo y) y
 *     se parten en tramos contiguos (mismo y, x consecutivo). Un tramo se
 *     "pela" de afuera hacia adentro: sacar el extremo izquierdo y el
 *     derecho de a pares. Cada extremo, en el momento en que le toca
 *     salir, siempre tiene su lado exterior abierto (por ser el borde
 *     original del tramo o porque su vecino ya salió antes) -así que
 *     siempre está libre, sin importar el largo del tramo.
 *  3. Un tramo de largo impar deja UNA ficha suelta en el medio (ambos
 *     lados abiertos, libre, pero sin pareja dentro de su propio tramo).
 *     Esas fichas sueltas se acumulan en una bolsa global y se emparejan
 *     entre sí al final, respetando qué suelta tapa a cuál (ver
 *     [pairLeftoversSafely]: no alcanza con que dos sueltas no se tapen
 *     entre sí si una TERCERA, todavía pendiente, tapa a una de las dos).
 *
 * Como quitar fichas nunca puede "tapar" ni "bloquear" a otra (la libertad
 * es monótona: una posición libre sigue libre para cualquier subconjunto
 * más chico del tablero), cada pareja que arma este algoritmo sigue siendo
 * válida sin importar cuánto se demore en llegar su turno dentro del orden
 * final. Esto corre en tiempo lineal/lineal-log, sin backtracking, excepto
 * en el emparejamiento final de sueltas (acotado al tamaño de esa bolsa,
 * que es 0 en los layouts del catálogo -filas siempre de largo par-).
 *
 * Esto no garantiza que CUALQUIER secuencia de jugadas del jugador lleve
 * a ganar (eso es imposible de garantizar en Mahjong Solitario sin
 * restringir mucho el juego, y ningún clon comercial lo hace tampoco):
 * garantiza que EXISTE al menos un camino de victoria desde el reparto
 * inicial. Por eso el motor ofrece pistas y remezcla ante un bloqueo real.
 *
 * La misma función sirve para el reparto inicial (144 fichas) y para un
 * "remezclado" parcial (sólo las fichas que quedan en el tablero): a
 * [generate] no le importa si el conjunto de posiciones es el tablero
 * completo o un subconjunto, siempre que el tamaño coincida con la
 * cantidad de tipos de ficha recibidos.
 */
object BoardGenerator {

    class UnsolvableLayoutException(message: String) : IllegalStateException(message)

    data class GeneratedBoard(
        val tiles: List<Tile>,
        /**
         * Orden de ids que, jugado de a pares consecutivos (0-1, 2-3, ...),
         * limpia el tablero por completo. Se expone principalmente para
         * poder verificarlo en tests contra el motor real; la UI no lo usa
         * (usar esto como "solución" visible le sacaría la gracia al juego).
         */
        val constructiveClearOrder: List<TileId>,
    )

    fun generate(
        positions: List<Position>,
        tileTypes: List<TileType>,
        random: Random = Random.Default,
        startingId: Int = 0,
    ): GeneratedBoard {
        require(positions.size == tileTypes.size) {
            "Cantidad de posiciones (${positions.size}) y de fichas (${tileTypes.size}) debe coincidir"
        }
        require(positions.isNotEmpty()) { "No se puede generar un tablero vacío" }
        require(positions.size % 2 == 0) { "La cantidad de posiciones debe ser par" }

        val positionOrder = computeConstructiveRemovalOrder(positions, random)
        val pairGroups = buildPairGroups(tileTypes, random)

        var nextId = startingId
        val tiles = ArrayList<Tile>(positions.size)
        for (i in pairGroups.indices) {
            val posA = positionOrder[2 * i]
            val posB = positionOrder[2 * i + 1]
            val (typeA, typeB) = pairGroups[i]
            tiles += Tile(TileId(nextId++), typeA, posA)
            tiles += Tile(TileId(nextId++), typeB, posB)
        }
        return GeneratedBoard(tiles = tiles, constructiveClearOrder = tiles.map { it.id })
    }

    /**
     * Calcula un orden de posiciones tal que retirarlas de a pares, en ese
     * orden, siempre es válido. Ver el KDoc de la clase para la explicación
     * completa del algoritmo (pelar filas de arriba hacia abajo).
     */
    private fun computeConstructiveRemovalOrder(positions: List<Position>, random: Random): List<Position> {
        val order = ArrayList<Position>(positions.size)
        val leftovers = ArrayList<Position>()

        val layersDescending = positions.map { it.z }.distinct().sortedDescending()
        for (z in layersDescending) {
            val rowsAtLayer = positions.filter { it.z == z }.groupBy { it.y }
            for (rowTiles in rowsAtLayer.values) {
                val sorted = rowTiles.sortedBy { it.x }
                var i = 0
                while (i < sorted.size) {
                    var j = i
                    while (j + 1 < sorted.size && sorted[j + 1].x == sorted[j].x + Position.TILE_SPAN) j++
                    // sorted[i..j] es un tramo contiguo: se pela de afuera hacia adentro.
                    var lo = i
                    var hi = j
                    while (lo < hi) {
                        if (random.nextBoolean()) {
                            order.add(sorted[lo]); order.add(sorted[hi])
                        } else {
                            order.add(sorted[hi]); order.add(sorted[lo])
                        }
                        lo++; hi--
                    }
                    if (lo == hi) leftovers.add(sorted[lo]) // tramo de largo impar: 1 ficha suelta
                    i = j + 1
                }
            }
        }

        val pairedLeftovers = pairLeftoversSafely(leftovers, random)
            ?: throw UnsolvableLayoutException(
                "No se encontró forma de emparejar ${leftovers.size} ficha(s) suelta(s) sin que se tapen entre sí; " +
                    "el layout no admite una secuencia de retiro completa.",
            )
        order.addAll(pairedLeftovers)
        return order
    }

    /**
     * Empareja las fichas "sueltas" (ambos lados libres dentro de su
     * propia fila, pero sin pareja ahí mismo) respetando el único orden
     * que importa entre ellas: si una suelta de una capa más alta tapa a
     * una de más abajo, la de arriba tiene que quedar afuera (ya
     * emparejada con otra cosa) ANTES de que le toque el turno a la de
     * abajo. No alcanza con chequear que el par elegido no se tape a sí
     * mismo: hay que chequear que NINGUNA otra suelta que siga pendiente
     * tape a la que se está por retirar (ver el bug real documentado en
     * el KDoc de la clase: dos sueltas que no se tapan entre sí igual
     * pueden ser inválidas si una tercera, todavía pendiente, tapa a una
     * de las dos).
     *
     * Estrategia: en cada paso, `first` es SIEMPRE una suelta de la capa
     * más alta que quede en el pool -por definición, nada pendiente puede
     * taparla-. Su pareja se elige entre las demás, descartando cualquier
     * candidata que esté tapada por ALGO que todavía siga en el pool
     * (backtracking si la primera candidata elegida no tiene ninguna
     * pareja válida más adelante). Devuelve null si no existe ningún
     * emparejamiento válido -sólo puede pasar en layouts degenerados,
     * ver el test correspondiente-.
     */
    private fun pairLeftoversSafely(leftovers: List<Position>, random: Random): List<Position>? {
        fun isCoveredByPending(candidate: Position, pool: List<Position>): Boolean =
            pool.any { it !== candidate && it.z > candidate.z && it.overlapsFootprint(candidate) }

        fun solve(pool: List<Position>): List<Position>? {
            if (pool.isEmpty()) return emptyList()
            val maxZ = pool.maxOf { it.z }
            val topCandidates = pool.filter { it.z == maxZ }
            val first = topCandidates[random.nextInt(topCandidates.size)]
            val rest = pool.filterNot { it === first }
            for (candidate in rest.shuffled(random)) {
                if (isCoveredByPending(candidate, pool)) continue
                val remainingPool = rest.filterNot { it === candidate }
                val sub = solve(remainingPool) ?: continue
                return listOf(first, candidate) + sub
            }
            return null
        }

        check(leftovers.size % 2 == 0) {
            "Cantidad impar de fichas sueltas (${leftovers.size}); esto sólo puede pasar si la cantidad " +
                "total de posiciones del layout es impar, lo cual ya debería haber sido rechazado antes."
        }
        return solve(leftovers)
    }

    /**
     * Agrupa la lista de tipos de ficha en 72 parejas "jugables juntas".
     * Los tipos comunes (palos, vientos, dragones) se emparejan con una
     * copia idéntica de sí mismos. Flores y estaciones -que hacen match
     * entre cualquier variante de su propia categoría- se agrupan de a
     * pares dentro de su propio pool, sin exigir que sean la misma flor.
     */
    private fun buildPairGroups(tileTypes: List<TileType>, random: Random): List<Pair<TileType, TileType>> {
        val flowers = tileTypes.filterIsInstance<TileType.FlowerTile>()
        val seasons = tileTypes.filterIsInstance<TileType.SeasonTile>()
        val exact = tileTypes.filterNot { it is TileType.FlowerTile || it is TileType.SeasonTile }

        val exactPairs = exact.groupBy { it }.values.flatMap { group ->
            require(group.size % 2 == 0) {
                "El tipo ${group.first()} aparece ${group.size} veces; debe ser una cantidad par para poder emparejarlo"
            }
            group.chunked(2).map { (a, b) -> a to b }
        }
        val flowerPairs = flowers.shuffled(random).chunked(2).map { (a, b) -> a to b }
        val seasonPairs = seasons.shuffled(random).chunked(2).map { (a, b) -> a to b }

        return (exactPairs + flowerPairs + seasonPairs).shuffled(random)
    }
}
