# NeoX Mahjong — CLASSICS NEOX

Módulo de Mahjong para CLASSICS NEOX, desarrollado de forma independiente
por Claude según las reglas del experimento (ver el brief original:
divergencia → integración → observación → auditoría). Este documento es
el entregable de la sección 13 del brief: decisión arquitectónica,
estructura, código, tests e integración.

## Qué juego es exactamente

**Mahjong Solitario** (a veces llamado "Shanghai" o "Turtle"): el
clásico de un jugador donde se retiran parejas de fichas iguales de un
tablero apilado en varias capas, no el Mahjong de mesa a 4 jugadores con
manos y puntuación por combinaciones. Se decidió así sin preguntar
(sección 10 del brief) porque es la lectura natural de "Mahjong" al lado
de Sudoku, Sopa de Letras, Dominó y Solitario en la misma app: los cinco
son juegos de sesión corta, para un jugador, sin necesitar una IA
oponente. Un Mahjong competitivo de 4 jugadores hubiera significado
construir también un motor de decisión de manos (yaku/fu) para 3
oponentes, un proyecto varias veces más grande que quedaba fuera de
"sesiones cortas" y "abrir y entender inmediatamente qué hacer" (sección
5 del brief).

---

## A. Decisión arquitectónica

**Tres módulos Gradle, no uno:**

- **`:core`** — Kotlin/JVM puro. Cero referencias a `android.*`. Todas
  las reglas del juego, la generación de tableros y el puntaje viven acá.
- **`:mahjong`** — librería Android (`com.android.library`). Compose,
  ViewModel, persistencia con DataStore, y el punto de integración
  público (`NeoXMahjongGame`). Depende de `:core`.
- **`:mahjong-demo`** — aplicación Android mínima. Sólo una `MainActivity` que
  monta `NeoXMahjongGame` a pantalla completa, para poder abrir y jugar
  el módulo solo. Cuando esto se integre a CLASSICS NEOX, `:mahjong-demo` no
  viaja — sólo `:core` y `:mahjong`.

**Por qué esta separación y no una sola app Android:** es la diferencia
entre "puedo escribir un test de la lógica" y "necesito un emulador para
saber si el juego calcula bien un match". Con `:core` aislado, las 43
pruebas de la sección D corren en menos de medio segundo, en cualquier
JVM, sin Android Studio ni emulador — y son precisamente las pruebas que
importan para la sección 12 del brief (corrección, testabilidad). También
es lo que permite que `:mahjong` sea, literalmente, el artefacto que
CLASSICS NEOX necesita importar: un módulo Android autocontenido con una
sola función pública de entrada, tal como pide la sección 6 del brief
("diseñá tu módulo de manera que posteriormente pueda conectarse a una
aplicación mayor").

**Patrón dentro de `:mahjong`:** MVVM + flujo unidireccional clásico de
Compose. `MahjongEngine` (en `:core`) es un objeto mutable síncrono, sin
saber nada de coroutines ni de Android; `MahjongViewModel` es la única
pieza que lo toca, y expone un `StateFlow<MahjongUiState>` de sólo
lectura. La UI nunca llama al motor directamente. Esto es deliberado y
no el patrón "genérico de cualquier app Android": significa que si el
día de mañana la integración de CLASSICS NEOX quisiera renderizar este
juego con otro toolkit de UI (o testearlo con Robolectric, o correrlo
headless), `MahjongEngine` no se entera ni cambia una línea.

**Generación de tableros — la decisión más importante del módulo:** en
vez de tirar fichas al azar sobre el layout y esperar que el tablero se
pueda resolver, `BoardGenerator` construye la partida "al revés": primero
calcula un orden de retiro completo que es válido por construcción
(pelando cada fila del tablero de afuera hacia adentro, de la capa más
alta a la más baja — ver el KDoc de `BoardGenerator.kt` para la
demostración completa), y recién después asigna qué ficha va en cada
lugar de manera que ese orden funcione. Esto **garantiza matemáticamente**
que todo tablero generado es resoluble desde el reparto inicial, algo que
un mazo puramente aleatorio no puede prometer (y de hecho una primera
versión con una heurística *greedy* más ingenua demostró, con sus propios
tests, que podía trabarse — quedó documentado en el propio código como
advertencia). El mismo generador se reutiliza para el botón "remezclar":
vuelve a correr el mismo algoritmo, ahora sólo sobre las posiciones que
quedan en el tablero, así que remezclar **siempre** devuelve un tablero
otra vez resoluble por completo.

**Layout propio, no una "tortuga" clásica copiada:** el layout incluido
("Pirámide NeoX") es un diseño original de 3 capas escalonadas y
centradas (80 + 48 + 16 = 144 fichas), generado programáticamente a
partir de rectángulos anidados en vez de transcribir a mano coordenadas
de un layout de otra implementación (la sección 6 del brief pide
explícitamente no copiar visualmente otra app; acá se tomó la misma
postura con la geometría del tablero). El sistema de layouts es un dato
(`BoardLayout` = lista de posiciones + una validación estructural), así
que sumar un segundo layout más adelante es agregar una función a
`LayoutCatalog`, no tocar el motor.

**Caras de ficha sin un solo asset de imagen:** las 144 fichas usan los
glifos reales del bloque Unicode "Mahjong Tiles" (U+1F000–U+1F02B) en vez
de un set de imágenes. Escala perfecto a cualquier tamaño sin pixelarse,
no necesita un pipeline de assets por densidad de pantalla, y cada ficha
igual lleva una etiqueta de texto de respaldo para accesibilidad y por si
alguna fuente muy vieja no dibuja el glifo.

**Puntaje y dificultad propios:** puntos base por match + bono por racha
(se corta al pedir pista o remezclar, ver `ScoreRules.kt`), pista y
remezclado con costo en puntos. Es un diseño propio, documentado y
ajustable en un solo archivo.

---

## B. Estructura

Estas tres carpetas se agregan a la raíz de `Classics-NeoX`, al lado de
lo que ya existe ahí (`app/` de Gemini, `README.md`, los `build.gradle.kts`
y `settings.gradle.kts` compartidos — ver la sección E para el patch
exacto de esos dos últimos):

```
Classics-NeoX/                                (raíz del repo — ya existente)
├── app/                                      ← ya existe (Gemini, Sopa de Letras)
├── core/                                     (Kotlin/JVM puro) — NUEVO
│   └── src/main/kotlin/.../core/
│       ├── model/       Position, TileType, Tile, BoardLayout
│       ├── layout/      LayoutCatalog (layouts embebidos)
│       ├── engine/      BoardTopology, BoardGenerator, MahjongEngine,
│       │                MoveResult, GameState, ScoreRules
│       └── persistence/ GameSnapshot, SnapshotMapper, GameStateRepository
│   └── src/test/kotlin/.../core/            (7 archivos, 43 tests — sección D)
│
├── mahjong/                                  (librería Android, Compose) — NUEVO
│   └── src/main/kotlin/.../
│       ├── MahjongEntryPoint.kt             ← punto de integración (sección E)
│       ├── NeoXMahjongModule.kt             ← adaptador de compatibilidad (sección E)
│       ├── ui/           MahjongScreen, BoardView, TileView, HudBar,
│       │                 GameDialogs, MahjongUiState, TileGlyphs, theme/
│       ├── viewmodel/    MahjongViewModel
│       └── persistence/  DataStoreGameStateRepository
│
└── mahjong-demo/                            (app mínima, harness standalone —
                                               nombrada distinto de `:app` para
                                               no pisar el shell de CLASSICS NEOX) — NUEVO
    └── src/main/kotlin/.../app/MainActivity.kt
```

## C. Código

El código completo (sin fragmentos, sin pseudocódigo, sin "el resto es
similar") está en los archivos del proyecto, no transcripto acá. Todo
`:core` está compilado y testeado contra un JDK 21 + Kotlin 2.0.21 reales
dentro de este mismo entorno (no es código "de memoria" sin verificar).
`:mahjong` y `:mahjong-demo` están escritos contra las APIs actuales de Jetpack
Compose / Material3 / DataStore, pero **no se pudieron compilar en este
entorno** porque no hay SDK de Android ni Android Gradle Plugin
instalados acá — sólo un JDK y el compilador de Kotlin standalone. Al
abrir el proyecto en Android Studio, un `Sync ahora` va a bajar el
wrapper de Gradle (dejé la versión fijada en
`gradle/wrapper/gradle-wrapper.properties`) y todas las dependencias.

## D. Tests

43 tests JUnit4 en `core/src/test`, todos en JVM pura (sin Robolectric).
Corridos dentro de este entorno con `kotlinc` + JUnit real:

```
TopologyTest            6 tests  — cobertura/bloqueo lateral/libertad
LayoutCatalogTest       5 tests  — 144 posiciones, sin duplicados, apoyo entre capas
BoardGeneratorTest      7 tests  — composición de fichas, resolubilidad, excepción en layout degenerado
MahjongEngineTest      13 tests  — selección/match/mismatch/undo/pista/pausa/victoria
ScoreRulesTest          5 tests  — puntaje base, racha, techo, penalidad con piso en 0
ShuffleTest             6 tests  — conserva fichas/posiciones, corta racha, respeta el límite
PersistenceTest         3 tests  — round-trip de encode/decode y de snapshot completo
──────────────────────────────
Total                  43 tests — OK (43 tests), 0 fallos
```

Además, fuera de la suite de JUnit, corrí un stress test ad-hoc con 2000
semillas del layout chico (verificadas contra un solver exhaustivo
independiente, no contra el propio generador), 500 semillas del tablero
de producción de 144 fichas (jugadas contra el motor real, no simuladas
aparte) y 300 partidas remezcladas 5 veces cada una: 0 fallos en las
tres corridas. Esto fue lo que encontró los dos bugs reales del primer
algoritmo de generación antes de este README — quedaron documentados en
el KDoc de `BoardGenerator.kt` como parte del historial de diseño, no
borrados.

**Cómo correrlos en Android Studio:** clic derecho sobre `core/src/test`
→ *Run Tests*, o `./gradlew :core:test` una vez generado el wrapper.

## E. Integración

Punto de entrada único, un composable — `MahjongEntryPoint.kt`:

```kotlin
@Composable
fun NeoXMahjongGame(
    repository: GameStateRepository,
    layout: BoardLayout = LayoutCatalog.PIRAMIDE_NEOX,
    onExit: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
)
```

Crea su propio `ViewModel` (sobrevive rotaciones solo, vía el ciclo de
vida real de Android) y restaura la última partida guardada en
`repository` la primera vez que se compone; si no hay ninguna, arranca
una nueva. `GameStateRepository` es la única interfaz que `:core` expone
para persistencia — CLASSICS NEOX puede pasarle la implementación con
DataStore que ya trae `:mahjong`, o una propia si quiere unificar el
guardado de los 5 juegos en un solo lugar.

El brief sugiere, como posible contrato, una forma imperativa de ciclo de
vida (`initialize/startGame/pauseGame/.../destroy`). Se evaluó y se
decidió **no** usarla como API principal: cosas como `initialize()` o
`loadState()` no tienen un rol real en Compose (el ViewModel se crea y
restaura solo), así que forzarlas hubiera significado métodos que
mienten sobre lo que hacen. En cambio, `NeoXMahjongModule.kt` ofrece esa
misma forma como un adaptador fino y opcional sobre `NeoXMahjongGame`,
para el caso de que el resto de CLASSICS NEOX prefiera integrar los 5
módulos con un ciclo de vida común imperativo. Ninguna lógica nueva vive
ahí: todo delega en el ViewModel real.

### Patch a los dos archivos raíz compartidos

Estos dos archivos ya existen en `Classics-NeoX` (los usa `app/`, de
Gemini). Acá **sí** hay que tocar líneas que ya existen, no sólo agregar:
la base técnica común que estuvimos definiendo (ver el hilo sobre "misma
versión para todos") quedó en la más reciente realmente vigente hoy —
Android Studio Quail 4 / AGP 9.4.0 —, verificada contra la documentación
oficial de Android (developer.android.com, con fecha), no adivinada:

```
AGP:            9.4.0   (setiembre 2026 — la actual)
Kotlin:         2.3.21
Compose BOM:    2026.08.00
compileSdk / targetSdk: 37   (Android 17, estable desde el 16/06/2026)
JDK / jvmToolchain: 17   (AGP 9.x sigue pidiendo mínimo 17, no subió)
minSdk:         24   (sin cambios — es elección de compatibilidad, no la toca esto)
```

Esto es un salto real desde el AGP 8.7.3 que hoy tiene la raíz — no es
sólo un número más alto: AGP 9 es versión mayor y sacó algunas APIs
viejas (nada que Mahjong ni, por lo que vi en el módulo de Gemini,
Sopa de Letras usen). Igual, cuando lo subas conviene que el módulo de
Gemini se vuelva a compilar una vez para confirmarlo — es un chequeo de
2 minutos en Android Studio, no una reescritura.

**`build.gradle.kts`** (raíz) — subir las 3 versiones que ya están y agregar 3 líneas:

```kotlin
plugins {
    id("com.android.application") version "9.4.0" apply false
    id("org.jetbrains.kotlin.android") version "2.3.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21" apply false
    // --- agregado para el módulo de Mahjong (Claude) ---
    id("com.android.library") version "9.4.0" apply false
    kotlin("jvm") version "2.3.21" apply false
    kotlin("plugin.serialization") version "2.3.21" apply false
}
```

**`gradle/wrapper/gradle-wrapper.properties`** (raíz) — AGP 9.4 pide Gradle
más nuevo que el que probablemente ya está apuntado; Android Studio te va
a ofrecer subirlo solo apenas abra el proyecto con el AGP nuevo (Tools →
Upgrade Assistant, o el cartel que aparece directo). No hace falta que lo
edites a mano si dejás que Android Studio lo resuelva.

**`settings.gradle.kts`** (raíz) — agregar 2-3 líneas al final:

```kotlin
include(":app")
// --- agregado para el módulo de Mahjong (Claude) ---
include(":core")
include(":mahjong")
include(":mahjong-demo")
```

(`:mahjong-demo` es opcional — sólo hace falta si querés poder abrir y
jugar el Mahjong solo, sin pasar por el shell de CLASSICS NEOX. Si no lo
subís, sacá esa carpeta y esa línea nomás.)

**Tabla de estado en el `README.md` raíz** — cambiar la fila de Claude:

```diff
- | Claude   | Mahjong     | Independent / to be delivered |
+ | Claude   | Mahjong     | 3-module (core/mahjong/mahjong-demo) + MVVM/UDF |
```

---

## Limitaciones conocidas y honestas

- **`:mahjong` y `:mahjong-demo` no se compilaron en este entorno** (sin SDK de
  Android acá). `:core` sí, completo, con sus 43 tests en verde.
- El layout **"Pirámide NeoX" es el único incluido**. El sistema soporta
  más (es un dato, no código nuevo por layout), pero armar una segunda
  geometría con buena terminación visual queda para una siguiente vuelta.
- El ícono de `:mahjong-demo` es un vector propio simple (una ficha estilizada),
  no un set de PNG por densidad — decisión consciente para no depender
  de generación de imágenes binarias en este entregable.
- Sin tests instrumentados de Compose (Espresso/`ComposeTestRule`): el
  riesgo real del módulo está en la lógica de `:core`, que es justamente
  la que quedó cubierta a fondo.
