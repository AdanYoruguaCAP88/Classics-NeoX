package com.classicsneox.crossword.core

enum class Direction(val dr:Int,val dc:Int){ ACROSS(0,1), DOWN(1,0); val perpendicular get()=if(this==ACROSS) DOWN else ACROSS }
data class CellPosition(val row:Int,val col:Int){ fun move(d:Direction,n:Int=1)=CellPosition(row+d.dr*n,col+d.dc*n) }
enum class Difficulty(val wordCount:Int,val maxSize:Int,val baseScore:Long,val revealFirst:Boolean){ EASY(8,13,1000,true),MEDIUM(11,15,2000,false),HARD(15,17,3000,false) }
data class WordEntry(val word:String,val clue:String){ init{require(word.isNotBlank()&&word.all{it in 'A'..'Z'||it=='Ñ'})} }
data class PlacedWord(val entry:WordEntry,val row:Int,val col:Int,val direction:Direction){ val length get()=entry.word.length; val start get()=CellPosition(row,col); fun positionAt(i:Int)=CellPosition(row+direction.dr*i,col+direction.dc*i); fun contains(p:CellPosition)=(0 until length).any{positionAt(it)==p} }
class CrosswordPuzzle(val width:Int,val height:Int,words:List<PlacedWord>,val solution:List<List<Char?>>,val seed:Long,val difficulty:Difficulty){
 val words=words.sortedWith(compareBy({it.row},{it.col},{it.direction.ordinal})); val cellNumbers:Map<CellPosition,Int>; private val byCell:Map<CellPosition,List<PlacedWord>>
 init{require(solution.size==height&&solution.all{it.size==width});val n=linkedMapOf<CellPosition,Int>();var x=1;for(r in 0 until height)for(c in 0 until width){val p=CellPosition(r,c);if(this.words.any{it.start==p})n[p]=x++};cellNumbers=n;byCell=buildMap{this@CrosswordPuzzle.words.forEach{w->for(i in 0 until w.length)getOrPut(w.positionAt(i)){mutableListOf()}.add(w)}}}
 fun charAt(p:CellPosition)=solution.getOrNull(p.row)?.getOrNull(p.col);fun isWhite(p:CellPosition)=charAt(p)!=null;fun wordsAt(p:CellPosition)=byCell[p].orEmpty();fun wordCells(w:PlacedWord)=(0 until w.length).map(w::positionAt);fun clueNumber(w:PlacedWord)=cellNumbers[w.start]?:0
}
