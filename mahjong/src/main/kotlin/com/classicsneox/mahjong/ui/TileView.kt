package com.classicsneox.mahjong.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.classicsneox.mahjong.ui.theme.MahjongPalette
import com.classicsneox.mahjong.ui.theme.MahjongType

/**
 * Una ficha física. El tamaño lo decide quien la usa ([BoardView] calcula
 * el ancho según el tablero); acá sólo se resuelve CÓMO se ve, no dónde
 * va -eso es responsabilidad exclusiva de [BoardView], vía `Modifier.offset`-.
 */
@Composable
fun TileView(
    tile: TileUiModel,
    width: Dp,
    height: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val elevation by animateDpAsState(if (tile.isFree) 4.dp else 1.dp, label = "tileElevation")
    val borderColor by animateColorAsState(
        when {
            tile.isSelected -> MahjongPalette.SelectedGlow
            tile.isHinted -> MahjongPalette.FreeTileGlow
            else -> MahjongPalette.TileEdge
        },
        label = "tileBorder",
    )
    val faceColor = if (tile.isFree) MahjongPalette.TileIvory else MahjongPalette.TileIvoryShadow
    val borderWidth = if (tile.isSelected || tile.isHinted) 2.5.dp else 1.dp
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .size(width, height)
            .shadow(elevation, shape, clip = false)
            .background(faceColor, shape)
            .border(borderWidth, borderColor, shape)
            .semantics { contentDescription = tile.fallbackLabel }
            .clickable(enabled = tile.isFree, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = tile.glyph, style = MahjongType.tileGlyph, color = tile.inkColor)
        if (!tile.isFree) {
            Box(Modifier.size(width, height).background(MahjongPalette.LockedOverlay, shape))
        }
    }
}
