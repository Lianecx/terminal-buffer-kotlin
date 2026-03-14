package terminalbuffer

import terminalbuffer.model.*
import kotlin.test.*

class ScreenTest {

    @Test
    fun `new screen is empty`() {
        val screen = Screen(5, 3)
        for (row in 0..<3) {
            assertEquals("", screen[row].toText())
        }
    }

    @Test
    fun `get row out of bounds throws`() {
        val screen = Screen(5, 3)
        assertFailsWith<IllegalArgumentException> { screen[-1] }
        assertFailsWith<IllegalArgumentException> { screen[3] }
    }

    @Test
    fun `can write and read cells`() {
        val screen = Screen(5, 3)
        screen[0][0] = Cell('A')
        screen[2][4] = Cell('Z')
        assertEquals(Cell('A'), screen[0][0])
        assertEquals(Cell('Z'), screen[2][4])
    }

    @Test
    fun `scrollUp returns evicted line and clears bottom`() {
        val screen = Screen(5, 3)
        screen[0][0] = Cell('A')
        screen[1][0] = Cell('B')
        screen[2][0] = Cell('C')

        val evicted = screen.scrollUp()
        assertEquals(Cell('A'), evicted[0])

        // After scroll: old row 1 is now row 0, old row 2 is now row 1, new row 2 is blank
        assertEquals(Cell('B'), screen[0][0])
        assertEquals(Cell('C'), screen[1][0])
        assertEquals(EMPTY_CELL, screen[2][0])
    }

    @Test
    fun `multiple scrolls maintain circular buffer invariant`() {
        val screen = Screen(3, 3)
        screen[0][0] = Cell('A')
        screen[1][0] = Cell('B')
        screen[2][0] = Cell('C')

        screen.scrollUp() // evicts A
        screen[2][0] = Cell('D')
        screen.scrollUp() // evicts B
        screen[2][0] = Cell('E')

        assertEquals(Cell('C'), screen[0][0])
        assertEquals(Cell('D'), screen[1][0])
        assertEquals(Cell('E'), screen[2][0])
    }

    @Test
    fun `scrollUp full cycle wraps around correctly`() {
        val screen = Screen(3, 3)
        // Scroll 3 times to fully rotate the circular buffer
        for (i in 0..<3) {
            screen[2][0] = Cell('A' + i)
            screen.scrollUp()
        }
        // After 3 scrolls, topRow is back to 0 (3 % 3 == 0)
        // All content should still be accessible
        screen[0][0] = Cell('X')
        assertEquals(Cell('X'), screen[0][0])
    }

    @Test
    fun `clear resets all lines`() {
        val screen = Screen(5, 3)
        screen[0][0] = Cell('A')
        screen[1][0] = Cell('B')
        screen[2][0] = Cell('C')
        screen.scrollUp() // offset topRow

        screen.clear()
        for (row in 0..<3) {
            assertEquals("", screen[row].toText())
        }
    }

    @Test
    fun `toText joins rows with newlines`() {
        val screen = Screen(5, 2)
        screen[0][0] = Cell('H')
        screen[0][1] = Cell('i')
        screen[1][0] = Cell('!')
        assertEquals("Hi\n!", screen.toText())
    }

    @Test
    fun `resizeLines shrink height evicts top lines`() {
        val screen = Screen(5, 4)
        screen[0][0] = Cell('A')
        screen[1][0] = Cell('B')
        screen[2][0] = Cell('C')
        screen[3][0] = Cell('D')

        val evicted = screen.resizeLines(5, 2)

        assertEquals(2, evicted.size)
        assertEquals(Cell('A'), evicted[0][0])
        assertEquals(Cell('B'), evicted[1][0])
        assertEquals(Cell('C'), screen[0][0])
        assertEquals(Cell('D'), screen[1][0])
    }

    @Test
    fun `resizeLines grow height adds blank lines at top`() {
        val screen = Screen(5, 2)
        screen[0][0] = Cell('A')
        screen[1][0] = Cell('B')

        val evicted = screen.resizeLines(5, 4)

        assertEquals(0, evicted.size)
        // Blank lines at top, existing content shifted down
        assertEquals("", screen[0].toText())
        assertEquals("", screen[1].toText())
        assertEquals(Cell('A'), screen[2][0])
        assertEquals(Cell('B'), screen[3][0])
    }

    @Test
    fun `resizeLines changes width`() {
        val screen = Screen(5, 2)
        screen[0][0] = Cell('A')
        screen[0][1] = Cell('B')
        screen[0][2] = Cell('C')
        screen[0][3] = Cell('D')
        screen[0][4] = Cell('E')

        screen.resizeLines(3, 2)

        assertEquals(Cell('A'), screen[0][0])
        assertEquals(Cell('B'), screen[0][1])
        assertEquals(Cell('C'), screen[0][2])
        assertFailsWith<IllegalArgumentException> { screen[0][3] } // width is now 3
    }

    @Test
    fun `fillFromScrollback replaces top lines`() {
        val screen = Screen(5, 4)
        screen[2][0] = Cell('C')
        screen[3][0] = Cell('D')

        val scrollbackLines = listOf(
            BufferLine(5).also { it[0] = Cell('X') },
            BufferLine(5).also { it[0] = Cell('Y') }
        )
        screen.fillFromScrollback(scrollbackLines)

        assertEquals(Cell('X'), screen[0][0])
        assertEquals(Cell('Y'), screen[1][0])
        assertEquals(Cell('C'), screen[2][0])
        assertEquals(Cell('D'), screen[3][0])
    }
}
