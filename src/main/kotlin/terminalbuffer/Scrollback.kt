package terminalbuffer

class Scrollback(private val maxSize: Int) {
    private val lines = ArrayDeque<BufferLine>()

    val size: Int get() = lines.size

    fun addLine(line: BufferLine) {
        if (maxSize <= 0) return
        if (lines.size >= maxSize) lines.removeFirst()
        lines.addLast(line)
    }

    operator fun get(index: Int): BufferLine {
        require(index in 0..<lines.size) { "Scrollback index $index out of bounds [0, ${lines.size})" }
        return lines[index]
    }

    fun clear() {
        lines.clear()
    }

    fun toText(): String {
        val sb = StringBuilder()
        for ((i, line) in lines.withIndex()) {
            if (i > 0) sb.append('\n')
            sb.append(line.toText())
        }
        return sb.toString()
    }

    /**
     * Removes and returns the last N lines from scrollback (most recent first, reversed to chronological order).
     * Used when screen height grows to pull lines back onto the screen.
     */
    fun removeLast(n: Int): List<BufferLine> {
        val count = minOf(n, lines.size)
        val result = mutableListOf<BufferLine>()
        repeat(count) {
            result.add(0, lines.removeLast())
        }
        return result
    }

    fun resizeLines(newWidth: Int) {
        for (i in lines.indices) lines[i] = lines[i].resized(newWidth)
    }
}
