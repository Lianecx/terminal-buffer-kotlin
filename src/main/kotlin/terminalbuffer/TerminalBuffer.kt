package terminalbuffer

import terminalbuffer.model.*

class TerminalBuffer(
    width: Int = 80,
    height: Int = 24,
    maxScrollback: Int = 1000
) {
    var width: Int = width
        private set
    var height: Int = height
        private set

    private var screen = Screen(width, height)
    private val scrollback = Scrollback(maxScrollback)

    private var cursorCol: Int = 0
    private var cursorRow: Int = 0
    private var currentAttributes = TextAttributes()

    // ── Attributes ──────────────────────────────────────────────

    fun setForeground(color: Color) {
        currentAttributes = currentAttributes.copy(foreground = color)
    }

    fun setBackground(color: Color) {
        currentAttributes = currentAttributes.copy(background = color)
    }

    fun addStyle(flag: StyleFlag) {
        currentAttributes = currentAttributes.copy(styles = currentAttributes.styles + flag)
    }

    fun removeStyle(flag: StyleFlag) {
        currentAttributes = currentAttributes.copy(styles = currentAttributes.styles - flag)
    }

    fun setStyles(styles: Set<StyleFlag>) {
        currentAttributes = currentAttributes.copy(styles = styles)
    }

    fun resetAttributes() {
        currentAttributes = TextAttributes()
    }

    fun getAttributes(): TextAttributes = currentAttributes

    // ── Cursor ──────────────────────────────────────────────────

    fun getCursor(): CursorPosition = CursorPosition(cursorCol, cursorRow)

    fun setCursor(col: Int, row: Int) {
        cursorCol = col.coerceIn(0, width - 1)
        cursorRow = row.coerceIn(0, height - 1)
    }

    fun moveCursorUp(n: Int = 1) {
        cursorRow = (cursorRow - n).coerceIn(0, height - 1)
    }

    fun moveCursorDown(n: Int = 1) {
        cursorRow = (cursorRow + n).coerceIn(0, height - 1)
    }

    fun moveCursorLeft(n: Int = 1) {
        cursorCol = (cursorCol - n).coerceIn(0, width - 1)
    }

    fun moveCursorRight(n: Int = 1) {
        cursorCol = (cursorCol + n).coerceIn(0, width - 1)
    }

    // ── Shared helpers ────────────────────────────────────────────

    /**
     * Converts a text string into a list of [Cell]s using the current attributes.
     * Wide characters produce two cells: one with [Cell.isWide] and one with [Cell.isWideContinuation].
     */
    private fun textToCells(text: String): List<Cell> {
        val cells = mutableListOf<Cell>()
        var offset = 0
        while (offset < text.length) {
            val codePoint = Character.codePointAt(text, offset)
            val charCount = Character.charCount(codePoint)
            val ch = if (charCount == 1) text[offset] else Character.highSurrogate(codePoint)

            if (WideCharUtils.isWideChar(codePoint)) {
                cells.add(Cell(ch, currentAttributes, isWide = true))
                cells.add(Cell('\u0000', currentAttributes, isWideContinuation = true))
            } else {
                cells.add(Cell(ch, currentAttributes))
            }
            offset += charCount
        }
        return cells
    }

    /**
     * If the cell at (col, row) is part of a wide character pair,
     * clear the partner cell to avoid orphaned halves.
     */
    private fun clearWideCharPartner(col: Int, row: Int) {
        val cell = screen[row][col]
        if (cell.isWide && col + 1 < width) screen[row][col + 1] = EMPTY_CELL
        if (cell.isWideContinuation && col > 0) screen[row][col - 1] = EMPTY_CELL
    }

    private fun advanceCursorForWrite() {
        cursorCol++
        if (cursorCol >= width) {
            cursorCol = 0
            if (cursorRow >= height - 1) insertLineAtBottom()
            else cursorRow++
        }
    }

    /**
     * Flattens all cells from (startCol, startRow) to the end of the screen into a list.
     */
    private fun flattenCellsFromCursor(startCol: Int, startRow: Int): MutableList<Cell> {
        val cells = mutableListOf<Cell>()
        for (row in startRow..<height) {
            val fromCol = if (row == startRow) startCol else 0
            for (col in fromCol..<width) cells.add(screen[row][col])
        }
        return cells
    }

    /**
     * Writes a list of cells onto the screen starting at (startCol, startRow),
     * scrolling as needed. Returns the number of scrolls that occurred.
     */
    private fun reflowCells(cells: List<Cell>, startCol: Int, startRow: Int): Int {
        var cellIdx = 0
        var row = startRow
        var col = startCol
        var scrollCount = 0

        while (cellIdx < cells.size && row < height) {
            screen[row][col] = cells[cellIdx]
            cellIdx++
            col++
            if (col >= width) {
                col = 0
                row++
                if (row >= height) {
                    insertLineAtBottom()
                    row = height - 1
                    scrollCount++
                }
            }
        }

        // Clear remaining cells on the last partially-filled line
        while (col < width && row < height && cellIdx >= cells.size) {
            screen[row][col] = EMPTY_CELL
            col++
        }

        return scrollCount
    }

    // ── Write ───────────────────────────────────────────────────

    /**
     * Writes text at the current cursor position, overwriting existing content.
     * Advances the cursor. Wraps at the right edge; scrolls when wrapping past the bottom.
     */
    fun write(text: String) {
        for (cell in textToCells(text)) {
            val isWide = cell.isWide
            if (cell.isWideContinuation) continue // skip continuation cells, handled with their primary

            if (isWide && cursorCol == width - 1) {
                // Wide char doesn't fit at last column — fill with space and wrap
                screen[cursorRow][cursorCol] = Cell(' ', currentAttributes)
                advanceCursorForWrite()
            }

            clearWideCharPartner(cursorCol, cursorRow)

            if (isWide) {
                if (cursorCol + 1 < width) clearWideCharPartner(cursorCol + 1, cursorRow)
                screen[cursorRow][cursorCol] = cell
                if (cursorCol + 1 < width) {
                    screen[cursorRow][cursorCol + 1] = Cell('\u0000', currentAttributes, isWideContinuation = true)
                }
                advanceCursorForWrite()
                advanceCursorForWrite()
            } else {
                screen[cursorRow][cursorCol] = cell
                advanceCursorForWrite()
            }
        }
    }

    // ── Insert ──────────────────────────────────────────────────

    /**
     * Inserts text at the current cursor position, shifting existing content to the right.
     * Content that shifts past the right edge wraps to the next line.
     * Content that falls off the bottom of the screen is lost (bottom line scrolls to scrollback).
     */
    fun insert(text: String) {
        if (cursorCol > 0 && screen[cursorRow][cursorCol].isWideContinuation) {
            screen[cursorRow][cursorCol - 1] = EMPTY_CELL
        }

        val existingCells = flattenCellsFromCursor(cursorCol, cursorRow)
        val newCells = textToCells(text)
        val combined = newCells + existingCells

        val scrollCount = reflowCells(combined, cursorCol, cursorRow)

        // Advance cursor past the inserted text, adjusting for scrolls
        var newCol = cursorCol + newCells.size
        var newRow = cursorRow - scrollCount
        while (newCol >= width) {
            newCol -= width
            newRow++
        }
        cursorCol = newCol.coerceIn(0, width - 1)
        cursorRow = newRow.coerceIn(0, height - 1)
    }

    // ── Delete ──────────────────────────────────────────────────

    /**
     * Deletes [count] cells at the current cursor position, shifting remaining content left.
     * Empty cells fill in from the right/bottom. Cursor position is unchanged.
     */
    fun delete(count: Int = 1) {
        if (count <= 0) return

        // Clear orphaned wide char partner at cursor
        if (cursorCol > 0 && screen[cursorRow][cursorCol].isWideContinuation) {
            screen[cursorRow][cursorCol - 1] = EMPTY_CELL
        }

        val existingCells = flattenCellsFromCursor(cursorCol, cursorRow)

        // Remove the first `count` cells (clamped to available)
        val removeCount = minOf(count, existingCells.size)
        val remaining = existingCells.subList(removeCount, existingCells.size)

        reflowCells(remaining, cursorCol, cursorRow)
    }

    // ── Fill Line ───────────────────────────────────────────────

    /**
     * Fills the entire line the cursor is on with the given character using current attributes.
     * Cursor position is unchanged.
     */
    fun fillLine(char: Char = ' ') {
        screen[cursorRow].fill(Cell(char, currentAttributes))
    }

    // ── Screen Operations (cursor/attribute independent) ────────

    /**
     * Scrolls the screen up by 1. Top line moves to scrollback.
     * A blank line appears at the bottom. Cursor position is unchanged.
     */
    fun insertLineAtBottom() {
        scrollback.addLine(screen.scrollUp())
    }

    /**
     * Clears all screen content. Cursor moves to (0,0). Scrollback is preserved.
     */
    fun clearScreen() {
        screen.clear()
        cursorCol = 0
        cursorRow = 0
    }

    /**
     * Clears screen and scrollback. Cursor moves to (0,0).
     */
    fun clearAll() {
        screen.clear()
        scrollback.clear()
        cursorCol = 0
        cursorRow = 0
    }

    // ── Content Access (Screen) ─────────────────────────────────

    fun getCell(col: Int, row: Int): Cell {
        require(col in 0..<width) { "Column $col out of bounds [0, $width)" }
        require(row in 0..<height) { "Row $row out of bounds [0, $height)" }
        return screen[row][col]
    }

    fun getChar(col: Int, row: Int): Char = getCell(col, row).char

    fun getAttributesAt(col: Int, row: Int): TextAttributes = getCell(col, row).attributes

    fun getScreenLine(row: Int): String {
        require(row in 0..<height) { "Row $row out of bounds [0, $height)" }
        return screen[row].toText()
    }

    fun getScreenContent(): String = screen.toText()

    // Content Access (Absolute: scrollback + screen)

    fun getScrollbackSize(): Int = scrollback.size

    fun getAbsoluteCell(col: Int, absoluteRow: Int): Cell {
        val scrollbackSize = scrollback.size
        return if (absoluteRow < scrollbackSize) {
            require(col in 0..<scrollback[absoluteRow].width) { "Column $col out of bounds" }
            scrollback[absoluteRow][col]
        } else {
            getCell(col, absoluteRow - scrollbackSize)
        }
    }

    fun getAbsoluteChar(col: Int, absoluteRow: Int): Char = getAbsoluteCell(col, absoluteRow).char

    fun getAbsoluteAttributesAt(col: Int, absoluteRow: Int): TextAttributes =
        getAbsoluteCell(col, absoluteRow).attributes

    fun getAbsoluteLine(absoluteRow: Int): String {
        val scrollbackSize = scrollback.size
        return if (absoluteRow < scrollbackSize) {
            scrollback[absoluteRow].toText()
        } else {
            getScreenLine(absoluteRow - scrollbackSize)
        }
    }

    fun getFullContent(): String {
        val scrollbackText = scrollback.toText()
        val screenText = screen.toText()
        return if (scrollbackText.isEmpty()) screenText
        else "$scrollbackText\n$screenText"
    }

    // ── Scrollback-only access ──────────────────────────────────

    fun getScrollbackLine(row: Int): String {
        require(row in 0..<scrollback.size) { "Scrollback row $row out of bounds [0, ${scrollback.size})" }
        return scrollback[row].toText()
    }

    // ── Resize ──────────────────────────────────────────────────

    /**
     * Resizes the screen to new dimensions.
     * - If height shrinks: top lines move to scrollback.
     * - If height grows: lines are pulled back from scrollback if available, otherwise blank.
     * - If width changes: lines are truncated or padded.
     * - Cursor is clamped to new bounds.
     */
    fun resize(newWidth: Int, newHeight: Int) {
        require(newWidth > 0) { "Width must be positive" }
        require(newHeight > 0) { "Height must be positive" }

        if (newWidth == width && newHeight == height) return

        // Resize scrollback lines if width changed
        if (newWidth != width) scrollback.resizeLines(newWidth)

        val oldHeight = height

        // Screen resize returns lines evicted from the top (when shrinking)
        val evicted = screen.resizeLines(newWidth, newHeight)
        for (line in evicted) scrollback.addLine(line)

        // If height grew, try to pull lines from scrollback
        if (newHeight > oldHeight) {
            val pullCount = minOf(newHeight - oldHeight, scrollback.size)
            if (pullCount > 0) {
                val pulled = scrollback.removeLast(pullCount)
                screen.fillFromScrollback(pulled)
                // Adjust cursor row to account for pulled lines
                cursorRow = (cursorRow + pullCount).coerceIn(0, newHeight - 1)
            }
        }

        width = newWidth
        height = newHeight

        // Clamp cursor
        cursorCol = cursorCol.coerceIn(0, width - 1)
        cursorRow = cursorRow.coerceIn(0, height - 1)
    }
}
