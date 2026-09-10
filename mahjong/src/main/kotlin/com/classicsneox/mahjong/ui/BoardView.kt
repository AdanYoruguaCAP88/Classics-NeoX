package com.classicsneox.mahjong.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.classicsneox.mahjong.core.model.Position
import com.classicsneox.mahjong.core.model.TileId

/**
 * Dibuja el tablero completo. La conversión de grilla a pantalla es
 * deliberadamente simple: cada unidad de grilla fina (ver
 * [Position.TILE_SPAN]) vale medio ancho/alto de ficha en pantalla, y
 * cada capa se corre un poquito hacia arriba-izquierda ([layerLift])
 * para leerse como "apilado" sin necesidad de un motor 3D real.
 *
 * Las fichas se dibujan en el orden que ya trae [tiles] (capas bajas
 * primero, ver `MahjongUiState.from`), así que una ficha de una capa
 * superior queda dibujada por encima de lo que tiene debajo sin tener
 * que tocar el z-index de Compose.
 */
@Composable
fun BoardView(
    tiles: List<TileUiModel>,
    onTileClick: (TileId) -> Unit,
    modifier: Modifier = Modifier,
    tileWidth: Dp = 34.dp,
    tileHeight: Dp = 44.dp,
    layerLift: Dp = 5.dp,
) {
    if (tiles.isEmpty()) return

    val maxGridX = tiles.maxOf { it.gridX }
    val maxGridY = tiles.maxOf { it.gridY }
    val halfUnitW = tileWidth / 2
    val halfUnitH = tileHeight / 2
    val boardWidth = halfUnitW * (maxGridX + Position.TILE_SPAN) + layerLift * 4
    val boardHeight = halfUnitH * (maxGridY + Position.TILE_SPAN) + layerLift * 4

    Box(modifier = modifier.size(boardWidth, boardHeight)) {
        for (tile in tiles) {
            val x = halfUnitW * tile.gridX - layerLift * tile.gridZ
            val y = halfUnitH * tile.gridY - layerLift * tile.gridZ
            TileView(
                tile = tile,
                width = tileWidth,
                height = tileHeight,
                onClick = { onTileClick(tile.id) },
                modifier = Modifier.offset(x = x, y = y),
            )
        }
    }
}
