package com.classicsneox.mahjong.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.classicsneox.mahjong.ui.theme.MahjongType

@Composable
fun WinDialog(score: Int, elapsedSeconds: Long, onPlayAgain: () -> Unit, onExit: (() -> Unit)?) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("¡Tablero limpio!", style = MahjongType.screenTitle) },
        text = { Text("Puntaje final: $score · Tiempo: ${elapsedSeconds / 60}:${(elapsedSeconds % 60).toString().padStart(2, '0')}") },
        confirmButton = { TextButton(onClick = onPlayAgain) { Text("Jugar de nuevo") } },
        dismissButton = onExit?.let { exit -> { TextButton(onClick = exit) { Text("Salir") } } },
    )
}

@Composable
fun LoseDialog(onShuffleOrRestart: () -> Unit, canShuffle: Boolean, onExit: (() -> Unit)?) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Sin movimientos", style = MahjongType.screenTitle) },
        text = { Text(if (canShuffle) "No quedan parejas disponibles. Podés remezclar." else "No quedan parejas ni remezclados disponibles.") },
        confirmButton = {
            TextButton(onClick = onShuffleOrRestart) { Text(if (canShuffle) "Remezclar" else "Reiniciar") }
        },
        dismissButton = onExit?.let { exit -> { TextButton(onClick = exit) { Text("Salir") } } },
    )
}

@Composable
fun PauseOverlayDialog(onResume: () -> Unit, onExit: (() -> Unit)?) {
    AlertDialog(
        onDismissRequest = onResume,
        title = { Text("Pausa", style = MahjongType.screenTitle) },
        text = { Text("El tiempo está detenido.") },
        confirmButton = { TextButton(onClick = onResume) { Text("Continuar") } },
        dismissButton = onExit?.let { exit -> { TextButton(onClick = exit) { Text("Salir") } } },
    )
}
