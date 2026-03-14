package terminalbuffer

import terminalbuffer.model.*
import kotlin.test.*

class DeleteTest {

    @Test
    fun `delete removes cells and shifts left`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("ABCDE")
        buf.setCursor(1, 0)
        buf.delete(2)
        assertEquals("ADE", buf.getScreenLine(0))
    }

    @Test
    fun `delete at start of line`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("ABCDE")
        buf.setCursor(0, 0)
        buf.delete(3)
        assertEquals("DE", buf.getScreenLine(0))
    }

    @Test
    fun `delete preserves cursor position`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("ABCDE")
        buf.setCursor(2, 0)
        buf.delete(1)
        assertEquals(CursorPosition(2, 0), buf.getCursor())
    }

    @Test
    fun `delete more than available clears rest of screen from cursor`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("ABCDE")
        buf.setCursor(3, 0)
        buf.delete(100)
        assertEquals("ABC", buf.getScreenLine(0))
    }

    @Test
    fun `delete zero does nothing`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("ABCDE")
        buf.setCursor(2, 0)
        buf.delete(0)
        assertEquals("ABCDE", buf.getScreenLine(0))
    }

    @Test
    fun `delete negative does nothing`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("ABCDE")
        buf.setCursor(2, 0)
        buf.delete(-1)
        assertEquals("ABCDE", buf.getScreenLine(0))
    }

    @Test
    fun `delete pulls content from next line`() {
        val buf = TerminalBuffer(5, 3)
        buf.write("ABCDEFGH")
        // Row 0: ABCDE, Row 1: FGH
        buf.setCursor(3, 0)
        buf.delete(2)
        // Should shift: ABCFG / H
        assertEquals("ABCFG", buf.getScreenLine(0))
        assertEquals("H", buf.getScreenLine(1))
    }

    @Test
    fun `delete single cell`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("ABCDE")
        buf.setCursor(0, 0)
        buf.delete(1)
        assertEquals("BCDE", buf.getScreenLine(0))
    }

    @Test
    fun `delete all content from start`() {
        val buf = TerminalBuffer(5, 2)
        buf.write("ABCDE")
        buf.setCursor(0, 0)
        buf.delete(5)
        assertEquals("", buf.getScreenLine(0))
    }
}
