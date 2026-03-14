package terminalbuffer

class Screen(var width: Int, var height: Int) {
    private var lines: Array<BufferLine> = Array(height) { BufferLine(width) }
    private var topRow: Int = 0

    private fun physicalIndex(logicalRow: Int): Int = (topRow + logicalRow) % height

    operator fun get(row: Int): BufferLine {
        require(row in 0..<height) { "Row $row out of bounds [0, $height)" }
        return lines[physicalIndex(row)]
    }

    /**
     * Scrolls the screen up by 1 line. Returns the line that scrolled off the top.
     * The new bottom line is cleared.
     */
    fun scrollUp(): BufferLine {
        val evicted = lines[topRow].copyOf()
        lines[topRow].clear()
        topRow = (topRow + 1) % height
        return evicted
    }

    fun clear() {
        for (line in lines) line.clear()
        topRow = 0
    }

    fun toText(): String {
        val sb = StringBuilder()
        for (row in 0..<height) {
            if (row > 0) sb.append('\n')
            sb.append(this[row].toText())
        }
        return sb.toString()
    }

    fun resizeLines(newWidth: Int, newHeight: Int): List<BufferLine> {
        // Collect current lines in logical order
        val currentLines = Array(height) { this[it] }

        val evicted = mutableListOf<BufferLine>()

        if (newHeight < height) {
            // Lines that don't fit go to scrollback (from the top)
            val excess = height - newHeight
            for (i in 0..<excess) {
                evicted.add(if (newWidth != width) currentLines[i].resized(newWidth) else currentLines[i].copyOf())
            }
            // Remaining lines become the new screen
            lines = Array(newHeight) { i ->
                val src = currentLines[i + excess]
                if (newWidth != width) src.resized(newWidth) else src.copyOf()
            }
        } else {
            // newHeight >= height: existing lines go to the bottom, blank lines fill the top pulled-from-scrollback slots
            // The caller (TerminalBuffer) handles pulling from scrollback; here we just pad with blanks at the top
            lines = Array(newHeight) { i ->
                val srcIdx = i - (newHeight - height)
                if (srcIdx < 0) {
                    BufferLine(newWidth)
                } else {
                    val src = currentLines[srcIdx]
                    if (newWidth != width) src.resized(newWidth) else src.copyOf()
                }
            }
        }

        width = newWidth
        height = newHeight
        topRow = 0

        return evicted
    }

    /**
     * Replaces the top N blank lines with lines pulled from scrollback.
     * Used during resize when height grows.
     */
    fun fillFromScrollback(scrollbackLines: List<BufferLine>) {
        for (i in scrollbackLines.indices) {
            if (i < height) lines[i].copyFrom(scrollbackLines[i])
        }
    }
}
