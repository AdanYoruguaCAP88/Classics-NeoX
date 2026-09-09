# CLASSICS NEOX

## Multi-Model Architecture Experiment

CLASSICS NEOX is a single Android application composed of five classic games. Each game is intentionally developed by a different AI model with independent architectural decisions.

| Model | Game | Architecture |
|---|---|---|
| Claude | Mahjong | Independent / to be delivered |
| Grok | Sudoku | Independent / to be delivered |
| Gemini | Word Search | MVI + Pure Kotlin Domain |
| ChatGPT | Domino | Independent / to be delivered |
| DeepSeek | Solitaire | Independent / to be delivered |

## Gemini — Word Search

The current repository contains the first integrated game: **Sopa de Letras**, implemented as an MVI-style presentation layer over a pure Kotlin game domain.

### Module

`com.classicsneox.wordsearch`

- `domain/models` — immutable game models
- `domain/engine` — grid generation and placement
- `presentation` — intents and state management
- `presentation/ui` — Jetpack Compose rendering and touch interaction
- `api` — integration entry point for the future Classics NeoX shell

The word grid uses one parent-level drag gesture handler and mathematical cell coordinates instead of individual clickable cells.

## Integration principle

The five game implementations remain architecturally independent. The future application shell should communicate with each game only through its public integration boundary and should not depend on its internal implementation.
