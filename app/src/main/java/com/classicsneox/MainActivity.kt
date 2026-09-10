package com.classicsneox

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.preferencesDataStore
import com.classicsneox.crossword.CrosswordScreen
import com.classicsneox.lightsout.LightsOutScreen
import com.classicsneox.mahjong.NeoXMahjongGame
import com.classicsneox.mahjong.persistence.DataStoreGameStateRepository
import com.classicsneox.solitaire.presentation.SolitaireScreen
import com.classicsneox.sudoku.SudokuScreen
import com.classicsneox.wordsearch.presentation.ui.WordSearchScreen

private val Context.mahjongDataStore by preferencesDataStore(name = "classics_neox_mahjong")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ClassicsNeoXApp() }
    }
}

private enum class GameDestination {
    HOME, SUDOKU, WORD_SEARCH, SOLITAIRE, CROSSWORD, LIGHTS_OUT, MAHJONG
}

@Composable
private fun ClassicsNeoXApp() {
    var destination by remember { mutableStateOf(GameDestination.HOME) }
    val goHome = { destination = GameDestination.HOME }

    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            when (destination) {
                GameDestination.HOME -> HomeScreen { destination = it }
                GameDestination.SUDOKU -> SudokuScreen(onBack = goHome)
                GameDestination.WORD_SEARCH -> GameShell(goHome) { WordSearchScreen() }
                GameDestination.SOLITAIRE -> GameShell(goHome) { SolitaireScreen() }
                GameDestination.CROSSWORD -> CrosswordScreen(onBack = goHome)
                GameDestination.LIGHTS_OUT -> LightsOutScreen(onBack = goHome)
                GameDestination.MAHJONG -> {
                    val context = LocalContext.current
                    NeoXMahjongGame(
                        repository = remember(context) { DataStoreGameStateRepository(context.mahjongDataStore) },
                        onExit = goHome,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun GameShell(
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TextButton(onClick = onBack, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text("‹ Volver")
        }
        Box(Modifier.fillMaxSize()) { content() }
    }
}

@Composable
private fun HomeScreen(onSelect: (GameDestination) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("CLASSICS NEOX", fontWeight = FontWeight.Bold, fontSize = 30.sp)
        Spacer(Modifier.height(6.dp))
        Text("Clásicos. Arquitecturas distintas. Un sistema.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(28.dp))

        GameButton("01  Sudoku", "Lógica · estrategia", GameDestination.SUDOKU, onSelect)
        GameButton("02  Sopa de Letras", "Palabras · exploración", GameDestination.WORD_SEARCH, onSelect)
        GameButton("03  Solitario", "Cartas · planificación", GameDestination.SOLITAIRE, onSelect)
        GameButton("04  Crucigrama · Kimi", "Palabras · deducción", GameDestination.CROSSWORD, onSelect)
        GameButton("05  Lights Out · Logos", "Lógica · transformación", GameDestination.LIGHTS_OUT, onSelect)
        GameButton("06  Mahjong · Claude", "Patrones · estrategia", GameDestination.MAHJONG, onSelect)

        Spacer(Modifier.height(22.dp))
        Text("Cada juego conserva su propia arquitectura de diseño.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun GameButton(
    title: String,
    subtitle: String,
    destination: GameDestination,
    onSelect: (GameDestination) -> Unit
) {
    Button(
        onClick = { onSelect(destination) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 13.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall)
            }
            Text("›", fontSize = 24.sp)
        }
    }
}
