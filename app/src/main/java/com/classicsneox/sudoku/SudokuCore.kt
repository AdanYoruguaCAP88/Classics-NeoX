package com.classicsneox.sudoku

import kotlin.random.Random

class Board private constructor(private val cells: IntArray) {
    companion object {
        const val CELL_COUNT = 81
        fun empty() = Board(IntArray(CELL_COUNT))
        fun from(values: IntArray): Board { require(values.size == CELL_COUNT); require(values.all { it in 0..9 }); return Board(values.copyOf()) }
    }
    fun get(index: Int) = cells[index]
    fun set(index: Int, value: Int): Board { require(index in 0..80 && value in 0..9); val next = cells.copyOf(); next[index] = value; return Board(next) }
    fun isComplete() = cells.none { it == 0 }
    fun toArray() = cells.copyOf()
    override fun equals(other: Any?) = other is Board && cells.contentEquals(other.cells)
    override fun hashCode() = cells.contentHashCode()
}

enum class Difficulty(val label: String, val clues: IntRange) { EASY("Fácil",40..46), MEDIUM("Medio",32..39), HARD("Difícil",26..31), EXPERT("Experto",22..25), MASTER("Maestro",17..21) }

data class Move(val index: Int, val value: Int, val previousValue: Int)

data class GameState(val puzzle: Board,val board: Board,val solution: Board,val fixed: BooleanArray,val difficulty: Difficulty,val history: List<Move> = emptyList(),val selectedIndex: Int = -1,val mistakes: Int = 0,val maxMistakes: Int = 3,val score: Int = 0,val elapsedMillis: Long = 0L,val completed: Boolean = false,val failed: Boolean = false,val paused: Boolean = false,val notes: Array<BooleanArray> = Array(81) { BooleanArray(10) }) { fun canEdit(index:Int)=index in 0..80&&!fixed[index]&&!completed&&!failed&&!paused }

object RuleValidator {
    fun valid(board: Board,index:Int,value:Int):Boolean { if(value==0)return true; val r=index/9; val c=index%9; for(x in 0..8)if(x!=c&&board.get(r*9+x)==value)return false; for(y in 0..8)if(y!=r&&board.get(y*9+c)==value)return false; val br=r/3*3; val bc=c/3*3; for(y in br until br+3)for(x in bc until bc+3){val i=y*9+x;if(i!=index&&board.get(i)==value)return false}; return true }
    fun solved(board:Board)=board.isComplete()&&(0 until 81).all{valid(board,it,board.get(it))}
}

object SudokuSolver {
    fun solve(board:Board,random:Random?=null):Board?{val a=board.toArray();return if(solveArray(a,random))Board.from(a)else null}
    fun countSolutions(board:Board,limit:Int=2):Int{val a=board.toArray();var count=0;fun search(){if(count>=limit)return;var best=-1;var bc=IntArray(0);for(i in 0..80)if(a[i]==0){val cs=candidates(a,i);if(cs.isEmpty())return;if(best==-1||cs.size<bc.size){best=i;bc=cs}};if(best==-1){count++;return};for(v in bc){a[best]=v;search();a[best]=0;if(count>=limit)return}};search();return count}
    private fun solveArray(a:IntArray,random:Random?):Boolean{var best=-1;var bc=IntArray(0);for(i in 0..80)if(a[i]==0){val cs=candidates(a,i);if(cs.isEmpty())return false;if(best==-1||cs.size<bc.size){best=i;bc=cs}};if(best==-1)return true;if(random!=null)bc.shuffle(random);for(v in bc){a[best]=v;if(solveArray(a,random))return true;a[best]=0};return false}
    private fun candidates(a:IntArray,index:Int):IntArray{val used=BooleanArray(10);val r=index/9;val c=index%9;for(x in 0..8)used[a[r*9+x]]=true;for(y in 0..8)used[a[y*9+c]]=true;val br=r/3*3;val bc=c/3*3;for(y in br until br+3)for(x in bc until bc+3)used[a[y*9+x]]=true;return(1..9).filter{!used[it]}.toIntArray()}
}

object SudokuGenerator {
    fun generate(difficulty:Difficulty,seed:Long=System.currentTimeMillis()):Pair<Board,Board>{val random=Random(seed);val solution=SudokuSolver.solve(Board.empty(),random)?:error("Unable to generate solution");val cells=solution.toArray();var clues=81;for(p in (0 until 81).shuffled(random)){if(clues<=difficulty.clues.first)break;val old=cells[p];cells[p]=0;if(SudokuSolver.countSolutions(Board.from(cells))==1)clues--else cells[p]=old};return Board.from(cells) to solution}
}

class SudokuEngine {
    fun newGame(d:Difficulty,seed:Long=System.currentTimeMillis()):GameState{val(p,s)=SudokuGenerator.generate(d,seed);return GameState(p,p,s,BooleanArray(81){p.get(it)!=0},d,score=baseScore(d))}
    fun select(s:GameState,i:Int)=if(i in 0..80)s.copy(selectedIndex=i)else s
    fun place(s:GameState,value:Int):GameState{val i=s.selectedIndex;if(!s.canEdit(i)||value !in 0..9)return s;val old=s.board.get(i);if(old==value)return s;val correct=value==0||s.solution.get(i)==value;val board=s.board.set(i,value);val mistakes=if(value!=0&&!correct)s.mistakes+1 else s.mistakes;val score=(s.score+if(value!=0&&correct)10 else if(!correct)-15 else 0).coerceAtLeast(0);val done=board==s.solution;return s.copy(board=board,history=s.history+Move(i,value,old),mistakes=mistakes,score=if(done)score+100 else score,completed=done,failed=mistakes>=s.maxMistakes)}
    fun undo(s:GameState):GameState{val m=s.history.lastOrNull()?:return s;return s.copy(board=s.board.set(m.index,m.previousValue),history=s.history.dropLast(1),selectedIndex=m.index)}
    fun restart(s:GameState)=s.copy(board=s.puzzle,history=emptyList(),mistakes=0,score=baseScore(s.difficulty),completed=false,failed=false,selectedIndex=-1,notes=Array(81){BooleanArray(10)},elapsedMillis=0L)
    fun togglePause(s:GameState)=s.copy(paused=!s.paused)
    fun toggleNote(s:GameState,n:Int):GameState{val i=s.selectedIndex;if(!s.canEdit(i)||n !in 1..9)return s;val notes=s.notes.map{it.copyOf()}.toTypedArray();notes[i][n]=!notes[i][n];return s.copy(notes=notes)}
    private fun baseScore(d:Difficulty)=when(d){Difficulty.EASY->100;Difficulty.MEDIUM->200;Difficulty.HARD->350;Difficulty.EXPERT->500;Difficulty.MASTER->800}
}

interface SudokuModule { fun startNewGame(difficulty:Difficulty=Difficulty.MEDIUM,seed:Long?=null):GameState;fun select(index:Int):GameState;fun place(value:Int):GameState;fun undo():GameState;fun restart():GameState;fun togglePause():GameState;fun toggleNote(number:Int):GameState;fun state():GameState }

class SudokuModuleImpl:SudokuModule{private val engine=SudokuEngine();private var current:GameState?=null;override fun startNewGame(difficulty:Difficulty,seed:Long?)=engine.newGame(difficulty,seed?:System.currentTimeMillis()).also{current=it};override fun select(index:Int)=update{engine.select(it,index)};override fun place(value:Int)=update{engine.place(it,value)};override fun undo()=update(engine::undo);override fun restart()=update(engine::restart);override fun togglePause()=update(engine::togglePause);override fun toggleNote(number:Int)=update{engine.toggleNote(it,number)};override fun state()=current?:startNewGame();private fun update(fn:(GameState)->GameState)=fn(current?:startNewGame()).also{current=it}}
