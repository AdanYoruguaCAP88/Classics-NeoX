package com.classicsneox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.classicsneox.crossword.CrosswordScreen
import com.classicsneox.lightsout.LightsOutScreen
import com.classicsneox.solitaire.presentation.SolitaireScreen
import com.classicsneox.sudoku.SudokuScreen
import com.classicsneox.wordsearch.presentation.ui.WordSearchScreen

enum class GameDestination {
    HOME, SUDOKU, WORD_SEARCH, SOLITAIRE, CROSSWORD, LIGHTS_OUT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ClassicsNeoXApp() }
    }
}

@Composable
private fun ClassicsNeoXApp() {
    var destination by remember { mutableStateOf(GameDestination.HOME) }

    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            when (destination) {
                GameDestination.HOME -> HomeScreen { destination = it }
                GameDestination.SUDOKU -> SudokuScreen(onBack = { destination = GameDestination.HOME })
                GameDestination.WORD_SEARCH -> GameFrame("Sopa de Letras", { destination = GameDestination.HOME }) {
                    WordSearchScreen()
                }
                GameDestination.SOLITAIRE -> GameFrame("Solitario", { destination = GameDestination.HOME }) {
                    SolitaireScreen()
                }
                GameDestination.CROSSWORD -> CrosswordScreen(onBack = { destination = GameDestination.HOME })
                GameDestination.LIGHTS_OUT -> LightsOutScreen(onBack = { destination = GameDestination.HOME })
            }
        }
    }
}

@Composable
private fun HomeScreen(onOpen: (GameDestination) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(18.dp))
        Text("CLASSICS", fontSize = 30.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Text("NEO X", fontSize = 30.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "Clásicos. Arquitecturas distintas. Un sistema.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(28.dp))

        GameCard("01", "Sudoku", "Lógica numérica", "SUDOKU") { onOpen(GameDestination.SUDOKU) }
        GameCard("02", "Sopa de Letras", "Búsqueda de palabras", "WORD SEARCH") { onOpen(GameDestination.WORD_SEARCH) }
        GameCard("03", "Solitario", "Cartas · estrategia", "SOLITAIRE") { onOpen(GameDestination.SOLITAIRE) }
        GameCard("04", "Crucigrama", "Kimi · palabras cruzadas", "CROSSWORD") { onOpen(GameDestination.CROSSWORD) }
        GameCard("05", "Lights Out", "Lógica · tablero", "LOGOS") { onOpen(GameDestination.LIGHTS_OUT) }

        Spacer(Modifier.height(18.dp))
        Text(
            "5 juegos disponibles · cada módulo conserva su propia identidad",
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(Modifier.weight(1f))
        Text("CLASSICS NEOX", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun GameCard(
    index: String,
    title: String,
    description: String,
    architecture: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    index,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
            Text(architecture, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun GameFrame(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("‹ Volver") }
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
        Box(Modifier.fillMaxSize()) { content() }
    }
}
