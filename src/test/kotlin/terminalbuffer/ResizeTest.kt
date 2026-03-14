package terminalbuffer

import terminalbuffer.model.*
import kotlin.test.*

class ResizeTest {

    @Test
    fun `resize to same dimensions is no-op`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("Hello")
        buf.setCursor(3, 2)
        buf.resize(10, 5)
        assertEquals("Hello", buf.getScreenLine(0))
        assertEquals(CursorPosition(3, 2), buf.getCursor())
    }

    @Test
    fun `resize requires positive dimensions`() {
        val buf = TerminalBuffer(10, 5)
        assertFailsWith<IllegalArgumentException> { buf.resize(0, 5) }
        assertFailsWith<IllegalArgumentException> { buf.resize(10, 0) }
        assertFailsWith<IllegalArgumentException> { buf.resize(-1, 5) }
    }

    // ── Height shrink ──────────────────────────────────────────

    @Test
    fun `shrink height moves top lines to scrollback`() {
        val buf = TerminalBuffer(10, 4)
        buf.write("AAAA")
        buf.setCursor(0, 1)
        buf.write("BBBB")
        buf.setCursor(0, 2)
        buf.write("CCCC")
        buf.setCursor(0, 3)
        buf.write("DDDD")

        buf.resize(10, 2)

        assertEquals(2, buf.getScrollbackSize())
        assertEquals("AAAA", buf.getScrollbackLine(0))
        assertEquals("BBBB", buf.getScrollbackLine(1))
        assertEquals("CCCC", buf.getScreenLine(0))
        assertEquals("DDDD", buf.getScreenLine(1))
    }

    @Test
    fun `shrink height clamps cursor`() {
        val buf = TerminalBuffer(10, 5)
        buf.setCursor(9, 4)
        buf.resize(10, 3)
        assertEquals(CursorPosition(9, 2), buf.getCursor())
    }

    // ── Height grow ────────────────────────────────────────────

    @Test
    fun `grow height pulls lines from scrollback`() {
        val buf = TerminalBuffer(10, 2)
        buf.write("AAAA")
        buf.setCursor(0, 1)
        buf.write("BBBB")
        buf.insertLineAtBottom() // AAAA -> scrollback

        assertEquals(1, buf.getScrollbackSize())
        buf.resize(10, 4)
        // AAAA should be pulled back from scrollback
        assertEquals(0, buf.getScrollbackSize())
    }

    @Test
    fun `grow height with no scrollback adds blank lines`() {
        val buf = TerminalBuffer(10, 2)
        buf.write("AAAA")
        buf.setCursor(0, 1)
        buf.write("BBBB")

        buf.resize(10, 4)
        assertEquals(4, buf.height)
        // Content should still be accessible
        assertTrue(buf.getFullContent().contains("AAAA"))
        assertTrue(buf.getFullContent().contains("BBBB"))
    }

    // ── Width changes ──────────────────────────────────────────

    @Test
    fun `shrink width truncates lines`() {
        val buf = TerminalBuffer(10, 3)
        buf.write("ABCDEFGHIJ")
        buf.resize(5, 3)
        assertEquals(5, buf.width)
        assertEquals("ABCDE", buf.getScreenLine(0))
    }

    @Test
    fun `grow width pads lines`() {
        val buf = TerminalBuffer(5, 3)
        buf.write("ABCDE")
        buf.resize(10, 3)
        assertEquals(10, buf.width)
        assertEquals("ABCDE", buf.getScreenLine(0))
        // Can now write at wider positions
        buf.setCursor(8, 0)
        buf.write("X")
        assertEquals('X', buf.getChar(8, 0))
    }

    @Test
    fun `width change clamps cursor`() {
        val buf = TerminalBuffer(10, 5)
        buf.setCursor(8, 0)
        buf.resize(5, 5)
        assertEquals(CursorPosition(4, 0), buf.getCursor())
    }

    @Test
    fun `width change resizes scrollback lines too`() {
        val buf = TerminalBuffer(10, 2)
        buf.write("ABCDEFGHIJ")
        buf.insertLineAtBottom()
        assertEquals("ABCDEFGHIJ", buf.getScrollbackLine(0))

        buf.resize(5, 2)
        assertEquals("ABCDE", buf.getScrollbackLine(0))
    }

    // ── Combined changes ───────────────────────────────────────

    @Test
    fun `simultaneous width and height change`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("Hello")
        buf.setCursor(0, 1)
        buf.write("World")

        buf.resize(6, 3)
        assertEquals(6, buf.width)
        assertEquals(3, buf.height)
        // 2 lines evicted to scrollback (height 5 -> 3), content shifted down
        assertEquals("Hello", buf.getScrollbackLine(0))
        assertEquals("World", buf.getScrollbackLine(1))
    }

    @Test
    fun `resize updates width and height properties`() {
        val buf = TerminalBuffer(10, 5)
        buf.resize(20, 30)
        assertEquals(20, buf.width)
        assertEquals(30, buf.height)
    }

    @Test
    fun `resize to 1x1`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("Hello")
        buf.resize(1, 1)
        assertEquals(1, buf.width)
        assertEquals(1, buf.height)
        assertEquals(CursorPosition(0, 0), buf.getCursor())
    }
}
