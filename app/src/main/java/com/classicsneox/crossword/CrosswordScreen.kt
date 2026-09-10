package com.classicsneox.crossword

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.classicsneox.crossword.core.*

@Composable
fun CrosswordScreen(onBack: () -> Unit) {
    val controller = remember { CrosswordController() }
    val state by controller.state.collectAsState()
    DisposableEffect(Unit) { onDispose { controller.destroy() } }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("‹ Volver") }
                Text("Crucigrama", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                Text("${state.score}")
            }
            Text("CLASSICS NEOX · ${state.session.elapsed / 60}:${(state.session.elapsed % 60).toString().padStart(2, '0')}")
            Spacer(Modifier.height(8.dp))
            Board(state, controller)
            Spacer(Modifier.height(8.dp))
            Clues(state, controller)
            Keyboard(controller)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(onClick = { controller.newGame() }, Modifier.weight(1f)) { Text("Nuevo") }
                Button(onClick = controller::check, Modifier.weight(1f)) { Text("Verificar") }
                Button(
                    onClick = controller::reveal,
                    Modifier.weight(1f),
                    enabled = state.revealsLeft > 0
                ) { Text("Pista ${state.revealsLeft}") }
            }
            if (state.solved) {
                Text(
                    "¡Resuelto! Puntaje ${state.score}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun Board(s: CrosswordUiState, c: CrosswordController) {
    val p = s.session.puzzle
    Column(Modifier.fillMaxWidth()) {
        for (r in 0 until p.height) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                for (col in 0 until p.width) {
                    val pos = CellPosition(r, col)
                    val ch = p.charAt(pos)
                    Box(
                        Modifier
                            .size(28.dp)
                            .background(
                                if (ch == null) MaterialTheme.colorScheme.onSurface
                                else if (pos in s.session.errors) MaterialTheme.colorScheme.errorContainer
                                else if (s.session.current == pos) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { if (ch != null) c.select(pos) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (ch != null) {
                            Text(s.session.grid[r][col]?.toString() ?: p.cellNumbers[pos]?.toString().orEmpty())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Clues(s: CrosswordUiState, c: CrosswordController) {
    Column(Modifier.fillMaxWidth()) {
        Text("Pistas", style = MaterialTheme.typography.titleMedium)
        s.session.puzzle.words.forEach { w ->
            Text(
                "${s.session.puzzle.clueNumber(w)}. ${w.entry.clue}",
                Modifier.fillMaxWidth().clickable { c.select(w.start) }.padding(3.dp)
            )
        }
    }
}

@Composable
private fun Keyboard(c: CrosswordController) {
    Column(Modifier.fillMaxWidth()) {
        ("ABCDEFGHIJKLMNÑOPQRSTUVWXYZ").chunked(7).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { ch ->
                    TextButton(onClick = { c.input(ch) }, Modifier.weight(1f)) { Text(ch.toString()) }
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            TextButton(onClick = c::erase, Modifier.weight(1f)) { Text("⌫") }
            TextButton(onClick = c::toggle, Modifier.weight(1f)) { Text("Dirección") }
        }
    }
}
