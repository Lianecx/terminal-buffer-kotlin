package terminalbuffer

import terminalbuffer.model.*
import kotlin.test.*

class ScrollbackTest {

    private fun lineWith(char: Char, width: Int = 5): BufferLine {
        val line = BufferLine(width)
        line[0] = Cell(char)
        return line
    }

    @Test
    fun `starts empty`() {
        val sb = Scrollback(10)
        assertEquals(0, sb.size)
    }

    @Test
    fun `addLine increases size`() {
        val sb = Scrollback(10)
        sb.addLine(lineWith('A'))
        assertEquals(1, sb.size)
        sb.addLine(lineWith('B'))
        assertEquals(2, sb.size)
    }

    @Test
    fun `get returns lines in insertion order`() {
        val sb = Scrollback(10)
        sb.addLine(lineWith('A'))
        sb.addLine(lineWith('B'))
        sb.addLine(lineWith('C'))
        assertEquals(Cell('A'), sb[0][0])
        assertEquals(Cell('B'), sb[1][0])
        assertEquals(Cell('C'), sb[2][0])
    }

    @Test
    fun `get out of bounds throws`() {
        val sb = Scrollback(10)
        sb.addLine(lineWith('A'))
        assertFailsWith<IllegalArgumentException> { sb[-1] }
        assertFailsWith<IllegalArgumentException> { sb[1] }
    }

    @Test
    fun `evicts oldest line when capacity reached`() {
        val sb = Scrollback(3)
        sb.addLine(lineWith('A'))
        sb.addLine(lineWith('B'))
        sb.addLine(lineWith('C'))
        assertEquals(3, sb.size)

        sb.addLine(lineWith('D'))
        assertEquals(3, sb.size)
        // A was evicted
        assertEquals(Cell('B'), sb[0][0])
        assertEquals(Cell('C'), sb[1][0])
        assertEquals(Cell('D'), sb[2][0])
    }

    @Test
    fun `maxSize zero never stores anything`() {
        val sb = Scrollback(0)
        sb.addLine(lineWith('A'))
        assertEquals(0, sb.size)
    }

    @Test
    fun `maxSize one keeps only last line`() {
        val sb = Scrollback(1)
        sb.addLine(lineWith('A'))
        sb.addLine(lineWith('B'))
        assertEquals(1, sb.size)
        assertEquals(Cell('B'), sb[0][0])
    }

    @Test
    fun `clear empties scrollback`() {
        val sb = Scrollback(10)
        sb.addLine(lineWith('A'))
        sb.addLine(lineWith('B'))
        sb.clear()
        assertEquals(0, sb.size)
    }

    @Test
    fun `toText joins lines`() {
        val sb = Scrollback(10)
        sb.addLine(lineWith('A'))
        sb.addLine(lineWith('B'))
        assertEquals("A\nB", sb.toText())
    }

    @Test
    fun `toText on empty returns empty string`() {
        val sb = Scrollback(10)
        assertEquals("", sb.toText())
    }

    @Test
    fun `removeLast returns most recent lines in chronological order`() {
        val sb = Scrollback(10)
        sb.addLine(lineWith('A'))
        sb.addLine(lineWith('B'))
        sb.addLine(lineWith('C'))

        val removed = sb.removeLast(2)
        assertEquals(2, removed.size)
        assertEquals(Cell('B'), removed[0][0]) // chronological: B before C
        assertEquals(Cell('C'), removed[1][0])
        assertEquals(1, sb.size)
        assertEquals(Cell('A'), sb[0][0])
    }

    @Test
    fun `removeLast more than available returns all`() {
        val sb = Scrollback(10)
        sb.addLine(lineWith('A'))
        val removed = sb.removeLast(5)
        assertEquals(1, removed.size)
        assertEquals(0, sb.size)
    }

    @Test
    fun `removeLast zero returns empty list`() {
        val sb = Scrollback(10)
        sb.addLine(lineWith('A'))
        val removed = sb.removeLast(0)
        assertEquals(0, removed.size)
        assertEquals(1, sb.size)
    }

    @Test
    fun `resizeLines changes width of all stored lines`() {
        val sb = Scrollback(10)
        sb.addLine(lineWith('A', 5))
        sb.addLine(lineWith('B', 5))
        sb.resizeLines(3)
        assertEquals(3, sb[0].width)
        assertEquals(3, sb[1].width)
        assertEquals(Cell('A'), sb[0][0])
    }
}
