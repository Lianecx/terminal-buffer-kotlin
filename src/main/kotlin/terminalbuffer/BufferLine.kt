package terminalbuffer

import terminalbuffer.model.Cell
import terminalbuffer.model.EMPTY_CELL

class BufferLine(val width: Int) {
    private val cells: Array<Cell> = Array(width) { EMPTY_CELL }

    operator fun get(col: Int): Cell {
        require(col in 0..<width) { "Column $col out of bounds [0, $width)" }
        return cells[col]
    }

    operator fun set(col: Int, cell: Cell) {
        require(col in 0..<width) { "Column $col out of bounds [0, $width)" }
        cells[col] = cell
    }

    fun fill(cell: Cell) {
        cells.fill(cell)
    }

    fun clear() {
        fill(EMPTY_CELL)
    }

    fun toText(): String {
        val sb = StringBuilder(width)
        for (cell in cells) {
            if (!cell.isWideContinuation) sb.append(cell.char)
        }
        return sb.toString().trimEnd()
    }

    fun insertCells(col: Int, count: Int) {
        if (count <= 0 || col >= width) return

        // Shift cells right from the end
        for (i in (width - 1) downTo (col + count)) cells[i] = cells[i - count]

        // Clear the opened gap
        for (i in col..<minOf(col + count, width)) cells[i] = EMPTY_CELL
    }

    fun copyFrom(other: BufferLine) {
        val copyLen = minOf(width, other.width)
        for (i in 0..<copyLen) cells[i] = other[i]
        for (i in copyLen..<width) cells[i] = EMPTY_CELL
    }

    fun copyOf(): BufferLine {
        val copy = BufferLine(width)
        for (i in 0..<width) copy.cells[i] = cells[i]
        return copy
    }

    fun resized(newWidth: Int): BufferLine {
        val newLine = BufferLine(newWidth)
        val copyLen = minOf(width, newWidth)
        for (i in 0..<copyLen) newLine.cells[i] = cells[i]
        return newLine
    }
}
