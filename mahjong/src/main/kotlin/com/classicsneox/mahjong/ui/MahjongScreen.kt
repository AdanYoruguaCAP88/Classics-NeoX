package com.classicsneox.mahjong.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.classicsneox.mahjong.core.model.TileId
import com.classicsneox.mahjong.ui.theme.MahjongPalette
import com.classicsneox.mahjong.ui.theme.NeoXMahjongTheme

/**
 * Pantalla completa de NeoX Mahjong. No conoce al ViewModel directamente
 * -recibe [state] y callbacks-: así queda fácil de previsualizar
 * (`@Preview`) y de testear en un `composeTestRule` sin necesitar un
 * ViewModel real ni Android en el classpath de test.
 */
@Composable
fun MahjongScreen(
    state: MahjongUiState,
    onTileClick: (TileId) -> Unit,
    onHint: () -> Unit,
    onShuffle: () -> Unit,
    onUndo: () -> Unit,
    onRestart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onExit: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    NeoXMahjongTheme {
        Scaffold(
            modifier = modifier,
            containerColor = MahjongPalette.TableFeltDark,
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                HudBar(
                    state = state,
                    onHint = onHint,
                    onShuffle = onShuffle,
                    onUndo = onUndo,
                    onRestart = onRestart,
                    onPause = onPause,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    BoardView(tiles = state.tiles, onTileClick = onTileClick)
                }
            }

            if (state.isWon) {
                WinDialog(score = state.score, elapsedSeconds = state.elapsedSeconds, onPlayAgain = onRestart, onExit = onExit)
            } else if (state.isLost) {
                LoseDialog(onShuffleOrRestart = if (state.canShuffle) onShuffle else onRestart, canShuffle = state.canShuffle, onExit = onExit)
            } else if (state.isPaused) {
                PauseOverlayDialog(onResume = onResume, onExit = onExit)
            }
        }
    }
}
