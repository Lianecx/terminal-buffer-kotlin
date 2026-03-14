package terminalbuffer

import terminalbuffer.model.*
import kotlin.test.*

class ContentAccessTest {

    // ── Screen access ──────────────────────────────────────────

    @Test
    fun `getCell returns correct cell`() {
        val buf = TerminalBuffer(10, 5)
        buf.setForeground(Color.RED)
        buf.write("A")
        val cell = buf.getCell(0, 0)
        assertEquals('A', cell.char)
        assertEquals(Color.RED, cell.attributes.foreground)
    }

    @Test
    fun `getCell out of bounds throws`() {
        val buf = TerminalBuffer(10, 5)
        assertFailsWith<IllegalArgumentException> { buf.getCell(-1, 0) }
        assertFailsWith<IllegalArgumentException> { buf.getCell(10, 0) }
        assertFailsWith<IllegalArgumentException> { buf.getCell(0, -1) }
        assertFailsWith<IllegalArgumentException> { buf.getCell(0, 5) }
    }

    @Test
    fun `getChar returns character`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("X")
        assertEquals('X', buf.getChar(0, 0))
    }

    @Test
    fun `getAttributesAt returns attributes`() {
        val buf = TerminalBuffer(10, 5)
        buf.setForeground(Color.CYAN)
        buf.addStyle(StyleFlag.BOLD)
        buf.write("A")
        val attrs = buf.getAttributesAt(0, 0)
        assertEquals(Color.CYAN, attrs.foreground)
        assertTrue(StyleFlag.BOLD in attrs.styles)
    }

    @Test
    fun `getScreenLine returns trimmed line`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("Hello")
        assertEquals("Hello", buf.getScreenLine(0))
    }

    @Test
    fun `getScreenLine of empty row returns empty string`() {
        val buf = TerminalBuffer(10, 5)
        assertEquals("", buf.getScreenLine(2))
    }

    @Test
    fun `getScreenLine out of bounds throws`() {
        val buf = TerminalBuffer(10, 5)
        assertFailsWith<IllegalArgumentException> { buf.getScreenLine(-1) }
        assertFailsWith<IllegalArgumentException> { buf.getScreenLine(5) }
    }

    @Test
    fun `getScreenContent returns all screen lines`() {
        val buf = TerminalBuffer(10, 3)
        buf.write("AAA")
        buf.setCursor(0, 1)
        buf.write("BBB")
        val content = buf.getScreenContent()
        assertTrue(content.contains("AAA"))
        assertTrue(content.contains("BBB"))
    }

    // ── Scrollback access ──────────────────────────────────────

    @Test
    fun `getScrollbackSize returns zero initially`() {
        val buf = TerminalBuffer(10, 5)
        assertEquals(0, buf.getScrollbackSize())
    }

    @Test
    fun `getScrollbackLine returns correct line`() {
        val buf = TerminalBuffer(10, 2)
        buf.write("ABCDE")
        buf.setCursor(0, 1)
        buf.write("FGHIJ")
        buf.insertLineAtBottom()

        assertEquals(1, buf.getScrollbackSize())
        assertEquals("ABCDE", buf.getScrollbackLine(0))
    }

    @Test
    fun `getScrollbackLine out of bounds throws`() {
        val buf = TerminalBuffer(10, 5)
        assertFailsWith<IllegalArgumentException> { buf.getScrollbackLine(0) }
    }

    // ── Absolute access ────────────────────────────────────────

    @Test
    fun `absolute access with no scrollback equals screen access`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("Hello")
        assertEquals(buf.getChar(0, 0), buf.getAbsoluteChar(0, 0))
        assertEquals(buf.getCell(0, 0), buf.getAbsoluteCell(0, 0))
    }

    @Test
    fun `absolute access spans scrollback and screen`() {
        val buf = TerminalBuffer(10, 2)
        buf.write("ABCDE")
        buf.setCursor(0, 1)
        buf.write("FGHIJ")
        buf.insertLineAtBottom()
        buf.setCursor(0, 1)
        buf.write("KLMNO")

        // Row 0 absolute = scrollback line "ABCDE"
        assertEquals('A', buf.getAbsoluteChar(0, 0))
        // Row 1 absolute = screen row 0 = "FGHIJ"
        assertEquals('F', buf.getAbsoluteChar(0, 1))
        // Row 2 absolute = screen row 1 = "KLMNO"
        assertEquals('K', buf.getAbsoluteChar(0, 2))
    }

    @Test
    fun `getAbsoluteLine works for scrollback and screen`() {
        val buf = TerminalBuffer(10, 2)
        buf.write("ABCDE")
        buf.setCursor(0, 1)
        buf.write("FGHIJ")
        buf.insertLineAtBottom()

        assertEquals("ABCDE", buf.getAbsoluteLine(0))
        assertEquals("FGHIJ", buf.getAbsoluteLine(1))
    }

    @Test
    fun `getAbsoluteAttributesAt returns correct attributes`() {
        val buf = TerminalBuffer(10, 2)
        buf.setForeground(Color.RED)
        buf.write("ABCDE")
        buf.setCursor(0, 1)
        buf.setForeground(Color.GREEN)
        buf.write("FGHIJ")
        buf.insertLineAtBottom()

        assertEquals(Color.RED, buf.getAbsoluteAttributesAt(0, 0).foreground)
        assertEquals(Color.GREEN, buf.getAbsoluteAttributesAt(0, 1).foreground)
    }

    @Test
    fun `getFullContent includes scrollback and screen`() {
        val buf = TerminalBuffer(10, 2)
        buf.write("ABCDE")
        buf.setCursor(0, 1)
        buf.write("FGHIJ")
        buf.insertLineAtBottom()

        val full = buf.getFullContent()
        assertTrue(full.contains("ABCDE"))
        assertTrue(full.contains("FGHIJ"))
    }

    @Test
    fun `getFullContent with no scrollback returns screen only`() {
        val buf = TerminalBuffer(10, 3)
        buf.write("Hello")
        val full = buf.getFullContent()
        assertTrue(full.contains("Hello"))
    }
}
