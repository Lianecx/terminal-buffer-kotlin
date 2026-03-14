package terminalbuffer

import terminalbuffer.model.*
import kotlin.test.*

class BufferLineTest {

    @Test
    fun `new line is filled with empty cells`() {
        val line = BufferLine(10)
        for (col in 0..<10) {
            assertEquals(EMPTY_CELL, line[col])
        }
    }

    @Test
    fun `get and set cells`() {
        val line = BufferLine(5)
        val cell = Cell('A', TextAttributes(foreground = Color.RED))
        line[0] = cell
        assertEquals(cell, line[0])
        assertEquals(EMPTY_CELL, line[1])
    }

    @Test
    fun `get out of bounds throws`() {
        val line = BufferLine(5)
        assertFailsWith<IllegalArgumentException> { line[-1] }
        assertFailsWith<IllegalArgumentException> { line[5] }
    }

    @Test
    fun `set out of bounds throws`() {
        val line = BufferLine(5)
        assertFailsWith<IllegalArgumentException> { line[-1] = EMPTY_CELL }
        assertFailsWith<IllegalArgumentException> { line[5] = EMPTY_CELL }
    }

    @Test
    fun `fill replaces all cells`() {
        val line = BufferLine(5)
        val cell = Cell('X')
        line.fill(cell)
        for (col in 0..<5) assertEquals(cell, line[col])
    }

    @Test
    fun `clear resets to empty cells`() {
        val line = BufferLine(5)
        line.fill(Cell('X'))
        line.clear()
        for (col in 0..<5) assertEquals(EMPTY_CELL, line[col])
    }

    @Test
    fun `toText returns trimmed string without continuation chars`() {
        val line = BufferLine(10)
        line[0] = Cell('H')
        line[1] = Cell('i')
        // Rest are spaces — should be trimmed
        assertEquals("Hi", line.toText())
    }

    @Test
    fun `toText skips wide continuation cells`() {
        val line = BufferLine(10)
        line[0] = Cell('\u4F60', isWide = true)  // 你
        line[1] = Cell('\u0000', isWideContinuation = true)
        line[2] = Cell('A')
        assertEquals("\u4F60A", line.toText())
    }

    @Test
    fun `toText on empty line returns empty string`() {
        val line = BufferLine(10)
        assertEquals("", line.toText())
    }

    @Test
    fun `insertCells shifts content right`() {
        val line = BufferLine(5)
        line[0] = Cell('A')
        line[1] = Cell('B')
        line[2] = Cell('C')
        line.insertCells(1, 1)
        assertEquals(Cell('A'), line[0])
        assertEquals(EMPTY_CELL, line[1])
        assertEquals(Cell('B'), line[2])
        assertEquals(Cell('C'), line[3])
    }

    @Test
    fun `insertCells at start pushes everything right`() {
        val line = BufferLine(4)
        line[0] = Cell('A')
        line[1] = Cell('B')
        line[2] = Cell('C')
        line[3] = Cell('D')
        line.insertCells(0, 2)
        assertEquals(EMPTY_CELL, line[0])
        assertEquals(EMPTY_CELL, line[1])
        assertEquals(Cell('A'), line[2])
        assertEquals(Cell('B'), line[3])
        // C and D fell off the end
    }

    @Test
    fun `insertCells with zero count does nothing`() {
        val line = BufferLine(3)
        line[0] = Cell('A')
        line.insertCells(0, 0)
        assertEquals(Cell('A'), line[0])
    }

    @Test
    fun `insertCells at end of line does nothing meaningful`() {
        val line = BufferLine(3)
        line[0] = Cell('A')
        line.insertCells(3, 1) // col == width, no-op
        assertEquals(Cell('A'), line[0])
    }

    @Test
    fun `copyFrom copies content and pads if wider`() {
        val src = BufferLine(3)
        src[0] = Cell('A')
        src[1] = Cell('B')
        src[2] = Cell('C')

        val dst = BufferLine(5)
        dst.fill(Cell('X'))
        dst.copyFrom(src)

        assertEquals(Cell('A'), dst[0])
        assertEquals(Cell('B'), dst[1])
        assertEquals(Cell('C'), dst[2])
        assertEquals(EMPTY_CELL, dst[3])
        assertEquals(EMPTY_CELL, dst[4])
    }

    @Test
    fun `copyFrom truncates if source is wider`() {
        val src = BufferLine(5)
        src[0] = Cell('A')
        src[1] = Cell('B')
        src[2] = Cell('C')
        src[3] = Cell('D')
        src[4] = Cell('E')

        val dst = BufferLine(3)
        dst.copyFrom(src)

        assertEquals(Cell('A'), dst[0])
        assertEquals(Cell('B'), dst[1])
        assertEquals(Cell('C'), dst[2])
    }

    @Test
    fun `copyOf creates independent copy`() {
        val line = BufferLine(3)
        line[0] = Cell('A')
        val copy = line.copyOf()
        assertEquals(Cell('A'), copy[0])

        copy[0] = Cell('Z')
        assertEquals(Cell('A'), line[0]) // original unchanged
    }

    @Test
    fun `resized to larger width pads with empty cells`() {
        val line = BufferLine(3)
        line[0] = Cell('A')
        line[1] = Cell('B')
        line[2] = Cell('C')

        val resized = line.resized(5)
        assertEquals(5, resized.width)
        assertEquals(Cell('A'), resized[0])
        assertEquals(Cell('B'), resized[1])
        assertEquals(Cell('C'), resized[2])
        assertEquals(EMPTY_CELL, resized[3])
        assertEquals(EMPTY_CELL, resized[4])
    }

    @Test
    fun `resized to smaller width truncates`() {
        val line = BufferLine(5)
        line[0] = Cell('A')
        line[1] = Cell('B')
        line[2] = Cell('C')
        line[3] = Cell('D')
        line[4] = Cell('E')

        val resized = line.resized(2)
        assertEquals(2, resized.width)
        assertEquals(Cell('A'), resized[0])
        assertEquals(Cell('B'), resized[1])
    }

    @Test
    fun `width 1 line works correctly`() {
        val line = BufferLine(1)
        assertEquals(EMPTY_CELL, line[0])
        line[0] = Cell('X')
        assertEquals("X", line.toText())
    }
}
