package terminalbuffer

import terminalbuffer.model.*
import kotlin.test.*

class WriteTest {

    @Test
    fun `write single character`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("A")
        assertEquals('A', buf.getChar(0, 0))
        assertEquals(CursorPosition(1, 0), buf.getCursor())
    }

    @Test
    fun `write string`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("Hello")
        assertEquals("Hello", buf.getScreenLine(0))
        assertEquals(CursorPosition(5, 0), buf.getCursor())
    }

    @Test
    fun `write overwrites existing content`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("AAAA")
        buf.setCursor(1, 0)
        buf.write("BB")
        assertEquals("ABBA", buf.getScreenLine(0))
    }

    @Test
    fun `write wraps at right edge`() {
        val buf = TerminalBuffer(5, 3)
        buf.write("HelloWorld")
        assertEquals("Hello", buf.getScreenLine(0))
        assertEquals("World", buf.getScreenLine(1))
        assertEquals(CursorPosition(0, 2), buf.getCursor())
    }

    @Test
    fun `write scrolls when wrapping past bottom`() {
        val buf = TerminalBuffer(5, 2)
        buf.write("HelloWorld")
        // "Hello" scrolled to scrollback, "World" is on row 0
        // cursor wrapped to row 1 (which caused a scroll), now on bottom line
        assertEquals(1, buf.getScrollbackSize())
        assertEquals("Hello", buf.getScrollbackLine(0))
    }

    @Test
    fun `write fills entire screen and scrolls`() {
        val buf = TerminalBuffer(3, 2)
        buf.write("ABCDEF")
        // ABC on row 0, DEF on row 1, cursor wraps causing scroll
        // After scroll: ABC -> scrollback, DEF -> row 0, blank row 1
        assertEquals("ABC", buf.getScrollbackLine(0))
        assertEquals("DEF", buf.getScreenLine(0))
    }

    @Test
    fun `write empty string does nothing`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("")
        assertEquals(CursorPosition(0, 0), buf.getCursor())
        assertEquals("", buf.getScreenLine(0))
    }

    @Test
    fun `write preserves attributes per cell`() {
        val buf = TerminalBuffer(10, 5)
        buf.setForeground(Color.RED)
        buf.write("R")
        buf.setForeground(Color.GREEN)
        buf.write("G")

        assertEquals(Color.RED, buf.getAttributesAt(0, 0).foreground)
        assertEquals(Color.GREEN, buf.getAttributesAt(1, 0).foreground)
    }

    @Test
    fun `write at specific cursor position`() {
        val buf = TerminalBuffer(10, 5)
        buf.setCursor(3, 2)
        buf.write("Hi")
        assertEquals("Hi", buf.getScreenLine(2).trim())
        assertEquals(CursorPosition(5, 2), buf.getCursor())
    }

    @Test
    fun `write single char at last column wraps to next line`() {
        val buf = TerminalBuffer(5, 3)
        buf.setCursor(4, 0)
        buf.write("AB")
        assertEquals('A', buf.getChar(4, 0))
        assertEquals('B', buf.getChar(0, 1))
        assertEquals(CursorPosition(1, 1), buf.getCursor())
    }

    @Test
    fun `write exactly fills a line`() {
        val buf = TerminalBuffer(5, 3)
        buf.write("ABCDE")
        assertEquals("ABCDE", buf.getScreenLine(0))
        // Cursor wraps to next line
        assertEquals(CursorPosition(0, 1), buf.getCursor())
    }

    @Test
    fun `multiple writes continue from cursor`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("Hello")
        buf.write(" World")
        assertEquals("Hello Worl", buf.getScreenLine(0))
        assertEquals("d", buf.getScreenLine(1))
    }

    @Test
    fun `write causes multiple scrolls`() {
        val buf = TerminalBuffer(3, 2)
        // 12 chars = 4 lines, only 2 fit on screen
        buf.write("ABCDEFGHIJKL")
        // Should have scrolled multiple times
        assertTrue(buf.getScrollbackSize() >= 2)
    }
}
