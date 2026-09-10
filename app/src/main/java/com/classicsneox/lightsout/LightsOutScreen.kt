package com.classicsneox.lightsout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LightsOutScreen(onBack: () -> Unit) {
    var state by remember { mutableStateOf(LightsOutEngine.newGame()) }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("‹ Volver") }
                Text("Lights Out", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Text("${state.moves} movimientos")
            }
            Spacer(Modifier.size(8.dp))
            Text("Apagá todas las luces. Cada toque cambia esa celda y sus vecinas.")
            Spacer(Modifier.size(20.dp))
            Board(state) { index -> state = LightsOutEngine.press(state, index) }
            Spacer(Modifier.size(20.dp))
            if (state.solved) {
                Text("¡Tablero resuelto!", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.size(8.dp))
            }
            Button(onClick = { state = LightsOutEngine.reset(state) }) { Text("Nuevo tablero") }
        }
    }
}

@Composable
private fun Board(state: LightsOutState, onPress: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(state.size) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(state.size) { col ->
                    val index = row * state.size + col
                    val lit = state.cells[index]
                    Box(
                        Modifier
                            .size(54.dp)
                            .background(
                                if (lit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onPress(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (lit) "●" else "○")
                    }
                }
            }
        }
    }
}
