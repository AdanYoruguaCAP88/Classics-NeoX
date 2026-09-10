package com.classicsneox.wordsearch.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.classicsneox.wordsearch.domain.models.GridCell
import com.classicsneox.wordsearch.domain.models.Point
import com.classicsneox.wordsearch.domain.models.Word
import com.classicsneox.wordsearch.presentation.WordSearchIntent
import com.classicsneox.wordsearch.presentation.WordSearchViewModel

@Composable
fun WordSearchScreen(viewModel: WordSearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.processIntent(WordSearchIntent.StartGame)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("SOPA DE LETRAS", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Puntuación: ${state.score}", fontSize = 18.sp)
        Spacer(Modifier.height(20.dp))

        if (state.grid.isNotEmpty()) {
            WordGrid(
                grid = state.grid,
                onDragStart = { viewModel.processIntent(WordSearchIntent.StartSelection(it)) },
                onDrag = { viewModel.processIntent(WordSearchIntent.DragSelection(it)) },
                onDragEnd = { viewModel.processIntent(WordSearchIntent.EndSelection) }
            )
        }

        Spacer(Modifier.height(20.dp))

        if (state.isGameOver) {
            Text("¡VICTORIA!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Button(onClick = { viewModel.processIntent(WordSearchIntent.StartGame) }) {
                Text("Nueva partida")
            }
        } else {
            WordList(words = state.words)
        }
    }
}

@Composable
fun WordGrid(
    grid: List<List<GridCell>>,
    onDragStart: (Point) -> Unit,
    onDrag: (Point) -> Unit,
    onDragEnd: () -> Unit
) {
    val gridSize = grid.size
    if (gridSize == 0) return

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(gridSize) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val cellSize = size.width.toFloat() / gridSize
                        val x = (offset.x / cellSize).toInt().coerceIn(0, gridSize - 1)
                        val y = (offset.y / cellSize).toInt().coerceIn(0, gridSize - 1)
                        onDragStart(Point(x, y))
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                    onDrag = { change, _ ->
                        val cellSize = size.width.toFloat() / gridSize
                        val x = (change.position.x / cellSize).toInt().coerceIn(0, gridSize - 1)
                        val y = (change.position.y / cellSize).toInt().coerceIn(0, gridSize - 1)
                        onDrag(Point(x, y))
                    }
                )
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            grid.forEach { row ->
                Row(Modifier.weight(1f)) {
                    val cellWeight = Modifier.weight(1f)
                    row.forEach { cell ->
                        Box(
                            modifier = cellWeight
                                .fillMaxHeight()
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        cell.isCurrentlySelected -> Color(0xFF5C6BC0).copy(alpha = 0.45f)
                                        cell.isPartOfFoundWord -> Color(0xFF66BB6A).copy(alpha = 0.55f)
                                        else -> Color.LightGray.copy(alpha = 0.25f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cell.char.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WordList(words: List<Word>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("Palabras", fontWeight = FontWeight.Bold)
        words.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                pair.forEach { word ->
                    Text(
                        text = if (word.isFound) "✓ ${word.text}" else word.text,
                        fontSize = 16.sp,
                        fontWeight = if (word.isFound) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
