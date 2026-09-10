package com.classicsneox.mahjong.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.preferencesDataStore
import com.classicsneox.mahjong.NeoXMahjongGame
import com.classicsneox.mahjong.persistence.DataStoreGameStateRepository

private val Context.mahjongDataStore by preferencesDataStore(name = "neox_mahjong_prefs")

/**
 * Harness standalone para poder abrir y jugar este módulo solo, sin
 * esperar a la app CLASSICS NEOX completa. Es intencionalmente lo más
 * fino posible: todo lo que hace es proveer un [DataStoreGameStateRepository]
 * concreto y montar [NeoXMahjongGame]. Cuando este módulo se integre a la
 * app real, esta Activity no viaja -sólo `:mahjong`, la librería, viaja-.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = DataStoreGameStateRepository(applicationContext.mahjongDataStore)
        setContent {
            NeoXMahjongGame(
                repository = repository,
                onExit = { finish() },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
