package terminalbuffer

import terminalbuffer.model.*
import kotlin.test.*

class InsertTest {

    @Test
    fun `insert shifts existing content right`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("World")
        buf.setCursor(0, 0)
        buf.insert("Hello ")
        assertEquals("Hello Worl", buf.getScreenLine(0))
        assertEquals("d", buf.getScreenLine(1))
    }

    @Test
    fun `insert at cursor position`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("AC")
        buf.setCursor(1, 0)
        buf.insert("B")
        assertEquals("ABC", buf.getScreenLine(0))
    }

    @Test
    fun `insert into empty buffer`() {
        val buf = TerminalBuffer(10, 5)
        buf.insert("Hello")
        assertEquals("Hello", buf.getScreenLine(0))
    }

    @Test
    fun `insert causes wrap to next line`() {
        val buf = TerminalBuffer(5, 3)
        buf.write("ABCDE")
        buf.setCursor(0, 0)
        buf.insert("XY")
        // XYABC on row 0, DE on row 1
        assertEquals("XYABC", buf.getScreenLine(0))
        assertEquals("DE", buf.getScreenLine(1))
    }

    @Test
    fun `insert causes scroll when content falls off bottom`() {
        val buf = TerminalBuffer(5, 2)
        buf.write("ABCDE")
        buf.setCursor(5, 0) // start of row 1
        buf.write("FGHIJ")
        // Screen full: ABCDE / FGHIJ
        buf.setCursor(0, 0)
        buf.insert("XX")
        // Content shifts: XXABC / DEFGH / IJ (IJ causes scroll)
        assertTrue(buf.getScrollbackSize() >= 1)
    }

    @Test
    fun `insert empty string does nothing`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("Hello")
        buf.setCursor(2, 0)
        buf.insert("")
        assertEquals("Hello", buf.getScreenLine(0))
        assertEquals(CursorPosition(2, 0), buf.getCursor())
    }

    @Test
    fun `insert preserves attributes of existing content`() {
        val buf = TerminalBuffer(10, 5)
        buf.setForeground(Color.RED)
        buf.write("R")
        buf.setCursor(0, 0)
        buf.setForeground(Color.GREEN)
        buf.insert("G")

        assertEquals(Color.GREEN, buf.getAttributesAt(0, 0).foreground)
        assertEquals(Color.RED, buf.getAttributesAt(1, 0).foreground)
    }

    @Test
    fun `insert advances cursor past inserted text`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("World")
        buf.setCursor(0, 0)
        buf.insert("Hello")
        assertEquals(CursorPosition(5, 0), buf.getCursor())
    }

    @Test
    fun `insert at end of content acts like append`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("Hello")
        // Cursor is at (5, 0) after write
        buf.insert(" World")
        assertEquals("Hello Worl", buf.getScreenLine(0))
        assertEquals("d", buf.getScreenLine(1))
    }
}
