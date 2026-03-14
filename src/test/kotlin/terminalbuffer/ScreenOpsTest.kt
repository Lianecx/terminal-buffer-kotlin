package terminalbuffer

import terminalbuffer.model.*
import kotlin.test.*

class ScreenOpsTest {

    @Test
    fun `insertLineAtBottom scrolls top line to scrollback`() {
        val buf = TerminalBuffer(10, 3)
        buf.write("AAAA")
        buf.setCursor(0, 1)
        buf.write("BBBB")
        buf.setCursor(0, 2)
        buf.write("CCCC")

        buf.insertLineAtBottom()

        assertEquals(1, buf.getScrollbackSize())
        assertEquals("AAAA", buf.getScrollbackLine(0))
        assertEquals("BBBB", buf.getScreenLine(0))
        assertEquals("CCCC", buf.getScreenLine(1))
        assertEquals("", buf.getScreenLine(2))
    }

    @Test
    fun `insertLineAtBottom multiple times`() {
        val buf = TerminalBuffer(10, 2)
        buf.write("AAAA")
        buf.setCursor(0, 1)
        buf.write("BBBB")

        buf.insertLineAtBottom()
        buf.insertLineAtBottom()

        assertEquals(2, buf.getScrollbackSize())
        assertEquals("AAAA", buf.getScrollbackLine(0))
        assertEquals("BBBB", buf.getScrollbackLine(1))
    }

    @Test
    fun `clearScreen empties screen and resets cursor`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("Hello")
        buf.setCursor(3, 2)
        buf.clearScreen()

        assertEquals(CursorPosition(0, 0), buf.getCursor())
        for (row in 0..<5) {
            assertEquals("", buf.getScreenLine(row))
        }
    }

    @Test
    fun `clearScreen preserves scrollback`() {
        val buf = TerminalBuffer(5, 2)
        buf.write("AAAAA")
        buf.insertLineAtBottom()
        assertEquals(1, buf.getScrollbackSize())

        buf.clearScreen()
        assertEquals(1, buf.getScrollbackSize())
        assertEquals("AAAAA", buf.getScrollbackLine(0))
    }

    @Test
    fun `clearAll empties screen and scrollback`() {
        val buf = TerminalBuffer(5, 2)
        buf.write("AAAAA")
        buf.insertLineAtBottom()
        buf.write("BBBBB")

        buf.clearAll()

        assertEquals(0, buf.getScrollbackSize())
        assertEquals(CursorPosition(0, 0), buf.getCursor())
        assertEquals("", buf.getScreenLine(0))
    }

    @Test
    fun `fillLine fills entire line with character`() {
        val buf = TerminalBuffer(5, 3)
        buf.setCursor(0, 1)
        buf.fillLine('-')

        assertEquals("", buf.getScreenLine(0))
        assertEquals("-----", buf.getScreenLine(1))
        assertEquals("", buf.getScreenLine(2))
    }

    @Test
    fun `fillLine with default fills with spaces`() {
        val buf = TerminalBuffer(5, 3)
        buf.setCursor(0, 1)
        buf.write("Hello")
        buf.setCursor(0, 1)
        buf.fillLine()

        assertEquals("", buf.getScreenLine(1)) // spaces are trimmed by toText
    }

    @Test
    fun `fillLine uses current attributes`() {
        val buf = TerminalBuffer(5, 3)
        buf.setCursor(0, 1)
        buf.setForeground(Color.RED)
        buf.fillLine('X')

        for (col in 0..<5) {
            assertEquals(Color.RED, buf.getAttributesAt(col, 1).foreground)
        }
    }

    @Test
    fun `fillLine does not move cursor`() {
        val buf = TerminalBuffer(10, 5)
        buf.setCursor(3, 2)
        buf.fillLine('X')
        assertEquals(CursorPosition(3, 2), buf.getCursor())
    }
}
