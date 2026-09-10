package com.classicsneox.mahjong.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.classicsneox.mahjong.ui.theme.MahjongPalette
import com.classicsneox.mahjong.ui.theme.MahjongType

@Composable
fun HudBar(
    state: MahjongUiState,
    onHint: () -> Unit,
    onShuffle: () -> Unit,
    onUndo: () -> Unit,
    onRestart: () -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            HudStat(label = "Puntaje", value = state.score.toString())
            HudStat(label = "Fichas", value = "${state.remainingTileCount}/${state.totalTileCount}")
            HudStat(label = "Racha", value = "x${state.streak}")
            HudStat(label = "Tiempo", value = formatElapsed(state.elapsedSeconds))
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            HudAction(Icons.Filled.Lightbulb, "Pista", onHint, enabled = state.hasAvailableMove)
            HudAction(Icons.Filled.Shuffle, "Remezclar", onShuffle, enabled = state.canShuffle)
            HudAction(Icons.Filled.Undo, "Deshacer", onUndo, enabled = state.canUndo)
            HudAction(Icons.Filled.Pause, "Pausar", onPause, enabled = !state.isPaused)
            HudAction(Icons.Filled.Refresh, "Reiniciar", onRestart, enabled = true)
        }
    }
}

@Composable
private fun HudStat(label: String, value: String) {
    Column {
        Text(label, style = MahjongType.hudLabel, color = MahjongPalette.TextOnFeltMuted)
        Text(value, style = MahjongType.hudValue, color = MahjongPalette.TextOnFelt)
    }
}

@Composable
private fun RowScope.HudAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (enabled) MahjongPalette.GoldLeafBright else MahjongPalette.TextOnFeltMuted,
        )
    }
}

private fun formatElapsed(totalSeconds: Long): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}
