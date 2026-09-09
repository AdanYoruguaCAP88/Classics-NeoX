package com.classicsneox.solitaire.presentation

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.classicsneox.solitaire.domain.*

@Composable
fun SolitaireScreen(vm: SolitaireViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val message by vm.message.collectAsState()
    Column(Modifier.fillMaxSize().padding(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Text("CLASSICS NEOX", fontWeight = FontWeight.Bold, fontSize = 18.sp); Text("Solitario", fontSize = 14.sp) }
            Column(horizontalAlignment = Alignment.End) { Text("Puntos ${state.score}"); Text(formatTime(state.elapsedSeconds), fontSize = 12.sp) }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            PileButton("Mazo\n${state.stock.size}") { vm.draw() }
            CardButton(state.waste.lastOrNull(), "Descarte") { if (state.waste.isNotEmpty()) vm.select(Source.Waste) }
            state.foundations.forEachIndexed { index, pile ->
                CardButton(pile.lastOrNull(), "F${index + 1}") { vm.moveToFoundation(index) }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            state.tableau.forEachIndexed { columnIndex, cards ->
                Column(Modifier.weight(1f).clickable { vm.moveToTableau(columnIndex) }, horizontalAlignment = Alignment.CenterHorizontally) {
                    if (cards.isEmpty()) EmptySlot()
                    else cards.forEachIndexed { cardIndex, card ->
                        MiniCard(card, Modifier.offset(y = (-cardIndex * 14).dp).clickable { vm.select(Source.Tableau(columnIndex, cardIndex)) })
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            OutlinedButton(onClick = vm::undo) { Text("Deshacer") }
            Button(onClick = vm::newGame) { Text("Nueva partida") }
        }
    }
    if (message != null) AlertDialog(onDismissRequest = vm::dismissMessage, confirmButton = { TextButton(onClick = vm::dismissMessage) { Text("OK") } }, title = { Text("Movimiento") }, text = { Text(message ?: "") })
    if (state.isComplete) AlertDialog(onDismissRequest = {}, confirmButton = { Button(onClick = vm::newGame) { Text("Jugar otra") } }, title = { Text("¡Ganaste!") }, text = { Text("Puntaje: ${state.score}") })
}

@Composable private fun PileButton(text: String, onClick: () -> Unit) = Box(Modifier.size(58.dp, 78.dp).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(7.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) { Text(text, fontSize = 11.sp) }

@Composable private fun CardButton(card: Card?, label: String, onClick: () -> Unit) = Box(Modifier.size(58.dp, 78.dp).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(7.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) { if (card == null) Text(label, fontSize = 10.sp) else MiniCard(card) }

@Composable private fun MiniCard(card: Card, modifier: Modifier = Modifier) {
    Box(modifier.size(48.dp, 64.dp).background(if (card.faceUp) Color.White else Color.DarkGray, RoundedCornerShape(5.dp)).border(1.dp, Color.Gray, RoundedCornerShape(5.dp)), contentAlignment = Alignment.Center) {
        if (card.faceUp) Text("${card.rank.label}${card.suit.symbol}", color = if (card.isRed) Color(0xFFB00020) else Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp) else Text("✦", color = Color.White)
    }
}

@Composable private fun EmptySlot() = Box(Modifier.size(48.dp, 64.dp).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(5.dp)), contentAlignment = Alignment.Center) { Text("K", fontSize = 11.sp) }
private fun formatTime(seconds: Long): String = "%02d:%02d".format(seconds / 60, seconds % 60)
