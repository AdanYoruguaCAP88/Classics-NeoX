package com.classicsneox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.classicsneox.sudoku.SudokuScreen
import com.classicsneox.wordsearch.presentation.ui.WordSearchScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ClassicsNeoXApp() }
    }
}

@Composable
private fun ClassicsNeoXApp() {
    var screen by remember { mutableStateOf("home") }
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            when (screen) {
                "sudoku" -> SudokuScreen(onBack = { screen = "home" })
                "wordsearch" -> Column(Modifier.fillMaxSize()) {
                    TextButton(onClick = { screen = "home" }, modifier = Modifier.padding(8.dp)) { Text("‹ Volver") }
                    WordSearchScreen()
                }
                else -> HomeScreen(
                    onSudoku = { screen = "sudoku" },
                    onWordSearch = { screen = "wordsearch" }
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(onSudoku: () -> Unit, onWordSearch: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("CLASSICS NEOX", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Clásicos. Cinco arquitecturas. Un sistema.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onSudoku, modifier = Modifier.fillMaxWidth()) { Text("Sudoku") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onWordSearch, modifier = Modifier.fillMaxWidth()) { Text("Sopa de Letras") }
        Spacer(Modifier.height(24.dp))
        Text("Mahjong · Dominó · Solitario — próximamente", style = MaterialTheme.typography.bodySmall)
    }
}
