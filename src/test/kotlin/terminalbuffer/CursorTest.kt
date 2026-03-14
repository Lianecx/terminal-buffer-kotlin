package terminalbuffer

import terminalbuffer.model.CursorPosition
import kotlin.test.*

class CursorTest {

    @Test
    fun `cursor starts at origin`() {
        val buf = TerminalBuffer(10, 5)
        assertEquals(CursorPosition(0, 0), buf.getCursor())
    }

    @Test
    fun `setCursor moves cursor`() {
        val buf = TerminalBuffer(10, 5)
        buf.setCursor(5, 3)
        assertEquals(CursorPosition(5, 3), buf.getCursor())
    }

    @Test
    fun `setCursor clamps to bounds`() {
        val buf = TerminalBuffer(10, 5)
        buf.setCursor(100, 100)
        assertEquals(CursorPosition(9, 4), buf.getCursor())
    }

    @Test
    fun `setCursor clamps negative values to zero`() {
        val buf = TerminalBuffer(10, 5)
        buf.setCursor(-5, -3)
        assertEquals(CursorPosition(0, 0), buf.getCursor())
    }

    @Test
    fun `moveCursorUp moves cursor up`() {
        val buf = TerminalBuffer(10, 5)
        buf.setCursor(0, 3)
        buf.moveCursorUp(2)
        assertEquals(CursorPosition(0, 1), buf.getCursor())
    }

    @Test
    fun `moveCursorUp clamps at top`() {
        val buf = TerminalBuffer(10, 5)
        buf.setCursor(0, 1)
        buf.moveCursorUp(5)
        assertEquals(CursorPosition(0, 0), buf.getCursor())
    }

    @Test
    fun `moveCursorDown moves cursor down`() {
        val buf = TerminalBuffer(10, 5)
        buf.moveCursorDown(3)
        assertEquals(CursorPosition(0, 3), buf.getCursor())
    }

    @Test
    fun `moveCursorDown clamps at bottom`() {
        val buf = TerminalBuffer(10, 5)
        buf.moveCursorDown(100)
        assertEquals(CursorPosition(0, 4), buf.getCursor())
    }

    @Test
    fun `moveCursorLeft moves cursor left`() {
        val buf = TerminalBuffer(10, 5)
        buf.setCursor(5, 0)
        buf.moveCursorLeft(3)
        assertEquals(CursorPosition(2, 0), buf.getCursor())
    }

    @Test
    fun `moveCursorLeft clamps at left edge`() {
        val buf = TerminalBuffer(10, 5)
        buf.setCursor(2, 0)
        buf.moveCursorLeft(10)
        assertEquals(CursorPosition(0, 0), buf.getCursor())
    }

    @Test
    fun `moveCursorRight moves cursor right`() {
        val buf = TerminalBuffer(10, 5)
        buf.moveCursorRight(5)
        assertEquals(CursorPosition(5, 0), buf.getCursor())
    }

    @Test
    fun `moveCursorRight clamps at right edge`() {
        val buf = TerminalBuffer(10, 5)
        buf.moveCursorRight(100)
        assertEquals(CursorPosition(9, 0), buf.getCursor())
    }

    @Test
    fun `default move amount is 1`() {
        val buf = TerminalBuffer(10, 5)
        buf.setCursor(5, 2)
        buf.moveCursorUp()
        assertEquals(CursorPosition(5, 1), buf.getCursor())
        buf.moveCursorDown()
        assertEquals(CursorPosition(5, 2), buf.getCursor())
        buf.moveCursorLeft()
        assertEquals(CursorPosition(4, 2), buf.getCursor())
        buf.moveCursorRight()
        assertEquals(CursorPosition(5, 2), buf.getCursor())
    }

    @Test
    fun `cursor at max bounds`() {
        val buf = TerminalBuffer(10, 5)
        buf.setCursor(9, 4)
        assertEquals(CursorPosition(9, 4), buf.getCursor())
    }
}
