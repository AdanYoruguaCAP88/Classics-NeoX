package com.classicsneox.mahjong

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.classicsneox.mahjong.core.model.BoardLayout
import com.classicsneox.mahjong.core.layout.LayoutCatalog
import com.classicsneox.mahjong.core.persistence.GameStateRepository
import com.classicsneox.mahjong.ui.MahjongScreen
import com.classicsneox.mahjong.viewmodel.MahjongViewModel

/**
 * ## Punto de entrada de NeoX Mahjong para CLASSICS NEOX
 *
 * Esta función es TODO lo que la capa de integración necesita conocer de
 * este módulo. No hace falta llamar nada más antes: crea su propio
 * ViewModel (atado al `NavBackStackEntry`/Activity que la hostee, con lo
 * cual sobrevive solo a cambios de configuración) y cuando se compone por
 * primera vez intenta restaurar la última partida guardada en
 * [repository]; si no hay ninguna, arranca una nueva.
 *
 * @param repository de dónde leer/guardar la partida. `:app` (el
 *   harness standalone de este repo) le pasa una implementación con
 *   DataStore; CLASSICS NEOX puede pasarle cualquier otra que cumpla el
 *   contrato de `:core` ([GameStateRepository]) -incluso una que
 *   comparta el mismo almacenamiento que usan los otros 4 juegos, si la
 *   capa de integración quiere unificar el guardado-.
 * @param layout qué tablero usar; por defecto el único que trae el
 *   catálogo hoy (ver [LayoutCatalog]).
 * @param onExit qué hacer cuando el jugador pide salir del juego (botón
 *   "Salir" en los diálogos de fin de partida/pausa). Si es null, esos
 *   diálogos simplemente no muestran esa opción -útil si CLASSICS NEOX
 *   monta este composable a pantalla completa sin una ruta "atrás" propia-.
 */
@Composable
fun NeoXMahjongGame(
    repository: GameStateRepository,
    layout: BoardLayout = LayoutCatalog.PIRAMIDE_NEOX,
    onExit: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val viewModel: MahjongViewModel = viewModel(factory = MahjongViewModel.Factory(repository, layout))
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    MahjongScreen(
        state = state,
        onTileClick = viewModel::onTileClick,
        onHint = viewModel::onHintClick,
        onShuffle = viewModel::onShuffleClick,
        onUndo = viewModel::onUndoClick,
        onRestart = viewModel::onRestartClick,
        onPause = viewModel::onPauseClick,
        onResume = viewModel::onResumeClick,
        onExit = onExit,
        modifier = modifier,
    )
}
