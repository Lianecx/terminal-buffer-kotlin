package terminalbuffer

import terminalbuffer.model.*
import kotlin.test.*

class WideCharIntegrationTest {

    @Test
    fun `write wide character occupies two cells`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("\u4F60") // 你
        val primary = buf.getCell(0, 0)
        val continuation = buf.getCell(1, 0)

        assertTrue(primary.isWide)
        assertEquals('\u4F60', primary.char)
        assertTrue(continuation.isWideContinuation)
        assertEquals(CursorPosition(2, 0), buf.getCursor())
    }

    @Test
    fun `write two wide characters occupies four cells`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("\u4F60\u597D") // 你好
        assertEquals('\u4F60', buf.getCell(0, 0).char)
        assertTrue(buf.getCell(0, 0).isWide)
        assertTrue(buf.getCell(1, 0).isWideContinuation)
        assertEquals('\u597D', buf.getCell(2, 0).char)
        assertTrue(buf.getCell(2, 0).isWide)
        assertTrue(buf.getCell(3, 0).isWideContinuation)
        assertEquals(CursorPosition(4, 0), buf.getCursor())
    }

    @Test
    fun `wide char at last column wraps to next line`() {
        val buf = TerminalBuffer(5, 3)
        buf.setCursor(4, 0)
        buf.write("\u4F60") // 你 — doesn't fit at col 4 (needs 2 cells)

        // Col 4 should be a space (padding), wide char wraps to row 1
        assertEquals(' ', buf.getChar(4, 0))
        assertEquals('\u4F60', buf.getCell(0, 1).char)
        assertTrue(buf.getCell(0, 1).isWide)
        assertTrue(buf.getCell(1, 1).isWideContinuation)
    }

    @Test
    fun `overwriting first half of wide char clears continuation`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("\u4F60") // 你 at cells 0,1
        buf.setCursor(0, 0)
        buf.write("A") // overwrite first half

        assertEquals('A', buf.getChar(0, 0))
        assertFalse(buf.getCell(0, 0).isWide)
        // Continuation at col 1 should be cleared
        assertEquals(' ', buf.getChar(1, 0))
        assertFalse(buf.getCell(1, 0).isWideContinuation)
    }

    @Test
    fun `overwriting second half of wide char clears primary`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("\u4F60") // 你 at cells 0,1
        buf.setCursor(1, 0)
        buf.write("B") // overwrite continuation

        // Primary at col 0 should be cleared
        assertEquals(' ', buf.getChar(0, 0))
        assertFalse(buf.getCell(0, 0).isWide)
        assertEquals('B', buf.getChar(1, 0))
    }

    @Test
    fun `mixed ASCII and wide characters`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("A\u4F60B") // A + 你 + B
        assertEquals('A', buf.getChar(0, 0))
        assertEquals('\u4F60', buf.getCell(1, 0).char)
        assertTrue(buf.getCell(1, 0).isWide)
        assertTrue(buf.getCell(2, 0).isWideContinuation)
        assertEquals('B', buf.getChar(3, 0))
    }

    @Test
    fun `wide char toText skips continuation`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("\u4F60\u597D") // 你好
        assertEquals("\u4F60\u597D", buf.getScreenLine(0))
    }

    @Test
    fun `delete wide character removes both cells`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("\u4F60X") // 你X — cells: wide, cont, X
        buf.setCursor(0, 0)
        buf.delete(2) // remove both cells of wide char
        assertEquals("X", buf.getScreenLine(0))
    }

    @Test
    fun `insert before wide character shifts it intact`() {
        val buf = TerminalBuffer(10, 5)
        buf.write("\u4F60") // 你 at 0,1
        buf.setCursor(0, 0)
        buf.insert("AB")
        // AB + 你 = A B 你_wide 你_cont
        assertEquals('A', buf.getChar(0, 0))
        assertEquals('B', buf.getChar(1, 0))
        assertEquals('\u4F60', buf.getCell(2, 0).char)
        assertTrue(buf.getCell(2, 0).isWide)
        assertTrue(buf.getCell(3, 0).isWideContinuation)
    }

    @Test
    fun `wide characters wrap correctly during write`() {
        val buf = TerminalBuffer(5, 3)
        // Fill 4 cells with wide chars, then another wide char needs 2 cells
        buf.write("\u4F60\u597D\u4E16") // 你好世 = 6 cells
        // Row 0: 你好 (4 cells) + space padding at col 4 (世 doesn't fit)
        // Row 1: 世 (2 cells)
        assertEquals('\u4F60', buf.getCell(0, 0).char)
        assertEquals('\u597D', buf.getCell(2, 0).char)
        assertEquals('\u4E16', buf.getCell(0, 1).char)
    }

    @Test
    fun `wide char attributes are preserved`() {
        val buf = TerminalBuffer(10, 5)
        buf.setForeground(Color.RED)
        buf.write("\u4F60") // 你
        assertEquals(Color.RED, buf.getAttributesAt(0, 0).foreground)
        assertEquals(Color.RED, buf.getAttributesAt(1, 0).foreground)
    }
}
