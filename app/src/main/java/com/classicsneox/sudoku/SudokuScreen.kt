package com.classicsneox.sudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SudokuScreen(onBack: (() -> Unit)? = null) {
    val module = remember { SudokuModuleImpl() }
    var state by remember { mutableStateOf(module.startNewGame(Difficulty.MEDIUM)) }
    var difficultyMenu by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) TextButton(onClick = onBack) { Text("‹ Volver") }
                Spacer(Modifier.weight(1f))
                Text("SUDOKU", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { state = module.restart() }) { Text("Reiniciar") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box {
                    OutlinedButton(onClick = { difficultyMenu = true }) { Text(state.difficulty.label) }
                    DropdownMenu(expanded = difficultyMenu, onDismissRequest = { difficultyMenu = false }) {
                        Difficulty.values().forEach { d -> DropdownMenuItem(text = { Text(d.label) }, onClick = { state = module.startNewGame(d); difficultyMenu = false }) }
                    }
                }
                Text("Errores: ${state.mistakes}/${state.maxMistakes}")
                Text("Puntos: ${state.score}")
            }
            Spacer(Modifier.height(12.dp))
            SudokuBoard(state) { index -> state = module.select(index) }
            Spacer(Modifier.height(14.dp))
            NumberPad(onNumber = { state = module.place(it) }, onUndo = { state = module.undo() })
            if (state.completed) {
                Spacer(Modifier.height(10.dp)); Text("¡Sudoku completado! 🎉", fontWeight = FontWeight.Bold)
            } else if (state.failed) {
                Spacer(Modifier.height(10.dp)); Text("Partida terminada. Podés reiniciar.", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SudokuBoard(state: GameState, onSelect: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().aspectRatio(1f).border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(4.dp))) {
        for (row in 0..8) {
            Row(Modifier.weight(1f)) {
                for (col in 0..8) {
                    val i = row * 9 + col
                    val selected = i == state.selectedIndex
                    val fixed = state.fixed[i]
                    val same = state.selectedIndex >= 0 && state.board.get(i) == state.board.get(state.selectedIndex) && state.board.get(i) != 0
                    val right = if (col == 2 || col == 5) 2.dp else 0.5.dp
                    val bottom = if (row == 2 || row == 5) 2.dp else 0.5.dp
                    Box(Modifier.weight(1f).fillMaxHeight().background(if (selected) MaterialTheme.colorScheme.primaryContainer else if (same) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent).border(right, MaterialTheme.colorScheme.outline).clickable { onSelect(i) }, contentAlignment = Alignment.Center) {
                        val value = state.board.get(i)
                        if (value != 0) Text(value.toString(), fontSize = 22.sp, fontWeight = if (fixed) FontWeight.Bold else FontWeight.Normal)
                        else {
                            val notes = (1..9).filter { state.notes[i][it] }
                            if (notes.isNotEmpty()) Text(notes.joinToString(""), fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberPad(onNumber: (Int) -> Unit, onUndo: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (r in 0..2) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (c in 1..3) {
                    val n = r * 3 + c
                    FilledTonalButton(onClick = { onNumber(n) }, modifier = Modifier.size(58.dp), contentPadding = PaddingValues(0.dp)) { Text(n.toString(), fontSize = 19.sp) }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = onUndo, modifier = Modifier.size(width = 122.dp, height = 48.dp)) { Text("Deshacer") }
            FilledTonalButton(onClick = { onNumber(0) }, modifier = Modifier.size(width = 122.dp, height = 48.dp)) { Text("Borrar") }
        }
    }
}
