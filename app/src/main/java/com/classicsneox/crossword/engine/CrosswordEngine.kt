package com.classicsneox.crossword.engine

import com.classicsneox.crossword.core.*

data class CrosswordSession(
    val puzzle: CrosswordPuzzle,
    val grid: List<List<Char?>>,
    val current: CellPosition?,
    val direction: Direction,
    val revealed: Set<CellPosition> = emptySet(),
    val errors: Set<CellPosition> = emptySet(),
    val elapsed: Long = 0,
    val checks: Int = 0,
    val initialReveals: Int = 0
)

object CrosswordEngine {
    fun newSession(p: CrosswordPuzzle, revealFirst: Boolean): CrosswordSession {
        val g = p.solution.map { it.map { null as Char? }.toMutableList() }.toMutableList()
        val rev = mutableSetOf<CellPosition>()
        if (revealFirst) p.words.forEach {
            rev += it.start
            g[it.start.row][it.start.col] = it.entry.word.first()
        }
        val frozen = g.map { it.toList() }
        val w = p.words.first()
        return CrosswordSession(
            p,
            frozen,
            p.wordCells(w).firstOrNull { frozen[it.row][it.col] == null } ?: w.start,
            w.direction,
            rev,
            initialReveals = rev.size
        )
    }

    fun select(s: CrosswordSession, p: CellPosition): CrosswordSession {
        if (!s.puzzle.isWhite(p)) return s
        val ws = s.puzzle.wordsAt(p)
        val d = if (s.current == p && ws.any { it.direction != s.direction }) {
            s.direction.perpendicular
        } else {
            ws.firstOrNull { it.direction == Direction.ACROSS }?.direction ?: ws.first().direction
        }
        return s.copy(current = p, direction = d)
    }

    fun toggle(s: CrosswordSession): CrosswordSession {
        val p = s.current ?: return s
        return if (s.puzzle.wordsAt(p).size > 1) s.copy(direction = s.direction.perpendicular) else s
    }

    fun input(s: CrosswordSession, ch: Char): CrosswordSession {
        val p = s.current ?: return s
        if (p in s.revealed) return s
        val g = s.grid.mapIndexed { r, row ->
            row.mapIndexed { c, v -> if (r == p.row && c == p.col) ch.uppercaseChar() else v }
        }
        val w = currentWord(s)
        val next = w?.let { p.move(s.direction).takeIf(it::contains) }
        return s.copy(grid = g, current = next ?: p, errors = s.errors - p)
    }

    fun erase(s: CrosswordSession): CrosswordSession {
        val p = s.current ?: return s
        if (p in s.revealed) return s
        val g = s.grid.mapIndexed { r, row ->
            row.mapIndexed { c, v -> if (r == p.row && c == p.col) null else v }
        }
        return s.copy(grid = g)
    }

    fun check(s: CrosswordSession): CrosswordSession {
        val e = mutableSetOf<CellPosition>()
        for (r in 0 until s.puzzle.height) for (c in 0 until s.puzzle.width) {
            val x = s.puzzle.solution[r][c] ?: continue
            if (s.grid[r][c] != x) e += CellPosition(r, c)
        }
        return s.copy(errors = e, checks = s.checks + 1)
    }

    fun reveal(s: CrosswordSession, p: CellPosition? = null): CrosswordSession {
        val target = p ?: s.current ?: return s
        val x = s.puzzle.charAt(target) ?: return s
        return s.copy(
            grid = s.grid.mapIndexed { r, row ->
                row.mapIndexed { c, v -> if (r == target.row && c == target.col) x else v }
            },
            revealed = s.revealed + target,
            errors = s.errors - target
        )
    }

    fun tick(s: CrosswordSession) = s.copy(elapsed = s.elapsed + 1)

    fun solved(s: CrosswordSession) = s.puzzle.solution.indices.all { r ->
        s.puzzle.solution[r].indices.all { c ->
            s.puzzle.solution[r][c] == null || s.grid[r][c] == s.puzzle.solution[r][c]
        }
    }

    fun currentWord(s: CrosswordSession) = s.current?.let { p ->
        s.puzzle.wordsAt(p).firstOrNull { it.direction == s.direction }
            ?: s.puzzle.wordsAt(p).firstOrNull()
    }
}
