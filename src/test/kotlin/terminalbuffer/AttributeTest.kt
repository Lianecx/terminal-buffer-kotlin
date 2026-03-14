package terminalbuffer

import terminalbuffer.model.*
import kotlin.test.*

class AttributeTest {

    @Test
    fun `default attributes are DEFAULT colors with no styles`() {
        val buf = TerminalBuffer(10, 5)
        val attrs = buf.getAttributes()
        assertEquals(Color.DEFAULT, attrs.foreground)
        assertEquals(Color.DEFAULT, attrs.background)
        assertTrue(attrs.styles.isEmpty())
    }

    @Test
    fun `setForeground changes foreground color`() {
        val buf = TerminalBuffer(10, 5)
        buf.setForeground(Color.RED)
        assertEquals(Color.RED, buf.getAttributes().foreground)
    }

    @Test
    fun `setBackground changes background color`() {
        val buf = TerminalBuffer(10, 5)
        buf.setBackground(Color.BLUE)
        assertEquals(Color.BLUE, buf.getAttributes().background)
    }

    @Test
    fun `addStyle adds a style flag`() {
        val buf = TerminalBuffer(10, 5)
        buf.addStyle(StyleFlag.BOLD)
        assertTrue(StyleFlag.BOLD in buf.getAttributes().styles)
    }

    @Test
    fun `addStyle is cumulative`() {
        val buf = TerminalBuffer(10, 5)
        buf.addStyle(StyleFlag.BOLD)
        buf.addStyle(StyleFlag.ITALIC)
        val styles = buf.getAttributes().styles
        assertTrue(StyleFlag.BOLD in styles)
        assertTrue(StyleFlag.ITALIC in styles)
    }

    @Test
    fun `removeStyle removes only the specified flag`() {
        val buf = TerminalBuffer(10, 5)
        buf.addStyle(StyleFlag.BOLD)
        buf.addStyle(StyleFlag.ITALIC)
        buf.removeStyle(StyleFlag.BOLD)
        val styles = buf.getAttributes().styles
        assertFalse(StyleFlag.BOLD in styles)
        assertTrue(StyleFlag.ITALIC in styles)
    }

    @Test
    fun `setStyles replaces all styles`() {
        val buf = TerminalBuffer(10, 5)
        buf.addStyle(StyleFlag.BOLD)
        buf.setStyles(setOf(StyleFlag.UNDERLINE, StyleFlag.DIM))
        val styles = buf.getAttributes().styles
        assertFalse(StyleFlag.BOLD in styles)
        assertTrue(StyleFlag.UNDERLINE in styles)
        assertTrue(StyleFlag.DIM in styles)
    }

    @Test
    fun `resetAttributes resets everything to defaults`() {
        val buf = TerminalBuffer(10, 5)
        buf.setForeground(Color.RED)
        buf.setBackground(Color.BLUE)
        buf.addStyle(StyleFlag.BOLD)
        buf.resetAttributes()
        assertEquals(TextAttributes(), buf.getAttributes())
    }

    @Test
    fun `written cells carry current attributes`() {
        val buf = TerminalBuffer(10, 5)
        buf.setForeground(Color.GREEN)
        buf.addStyle(StyleFlag.BOLD)
        buf.write("A")

        val cell = buf.getCell(0, 0)
        assertEquals('A', cell.char)
        assertEquals(Color.GREEN, cell.attributes.foreground)
        assertTrue(StyleFlag.BOLD in cell.attributes.styles)
    }

    @Test
    fun `attribute changes do not affect already written cells`() {
        val buf = TerminalBuffer(10, 5)
        buf.setForeground(Color.RED)
        buf.write("A")
        buf.setForeground(Color.BLUE)
        buf.write("B")

        assertEquals(Color.RED, buf.getCell(0, 0).attributes.foreground)
        assertEquals(Color.BLUE, buf.getCell(1, 0).attributes.foreground)
    }
}
