# CLASSICS NEOX

## Multi-Model Architecture Experiment

CLASSICS NEOX is a single Android application composed of five classic games. Each game is intentionally developed by a different AI model with independent architectural decisions.

| Model | Game | Architecture |
|---|---|---|
| Claude | Mahjong | Independent / to be delivered |
| **Grok** | **Sudoku** | **Immutable State + Pure Engine + Reactive Compose** |
| Gemini | Word Search | MVI + Pure Kotlin Domain |
| ChatGPT | Domino | Independent / to be delivered |
| DeepSeek | Solitaire | Independent / to be delivered |

## Integrated modules

### Grok — Sudoku

The Sudoku module is integrated under `com.classicsneox.sudoku`.

Its architectural signature is intentionally independent: immutable board values, a pure Sudoku engine/generator/solver, a minimal `SudokuModule` integration boundary, and a Jetpack Compose presentation layer.

The game includes:

- deterministic seeded generation;
- unique-solution verification;
- five difficulty levels;
- immutable board transitions;
- selection, number placement, erase and undo;
- notes;
- scoring and mistake limit;
- restart and pause state;
- Compose UI designed for touch interaction.

The implementation lives in:

`app/src/main/java/com/classicsneox/sudoku/`

### Gemini — Word Search

The current repository also contains **Sopa de Letras**, implemented as an MVI-style presentation layer over a pure Kotlin game domain.

### Application shell

`MainActivity` currently acts as the temporary Classics NeoX shell. It exposes the integrated Sudoku and Word Search modules while the remaining three independent game modules are incorporated.

## Integration principle

The five game implementations remain architecturally independent. The application shell communicates with each game through its public integration boundary and should not depend on internal implementation details.

The objective is not to homogenize the five architectures. The objective is to preserve their differences and study what emerges when independently designed systems coexist inside one product.
