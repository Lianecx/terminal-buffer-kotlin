package terminalbuffer.ui

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import terminalbuffer.TerminalBuffer
import terminalbuffer.model.Color
import terminalbuffer.model.StyleFlag

class TerminalBufferViewer(private val buffer: TerminalBuffer) {

    private var scrollOffset = 0

    fun show() {
        val factory = DefaultTerminalFactory()
        factory.setInitialTerminalSize(TerminalSize(
            maxOf(buffer.width, 80),
            buffer.height + STATUS_BAR_HEIGHT
        ))
        val terminal = factory.createTerminal()
        val screen = TerminalScreen(terminal)
        screen.startScreen()
        screen.cursorPosition = null

        try {
            render(screen)

            while (true) {
                // Poll for resize independently of input
                screen.doResizeIfNecessary()?.let { newSize ->
                    val newWidth = newSize.columns
                    val newHeight = maxOf(1, newSize.rows - STATUS_BAR_HEIGHT)
                    if (newWidth != buffer.width || newHeight != buffer.height) {
                        buffer.resize(newWidth, newHeight)
                        scrollOffset = scrollOffset.coerceAtMost(buffer.getScrollbackSize())
                    }
                    render(screen)
                }

                val key = screen.pollInput()
                if (key == null) {
                    Thread.sleep(16)
                    continue
                }

                when (key.keyType) {
                    KeyType.Escape, KeyType.EOF -> break
                    KeyType.Character -> {
                        if (key.character == 'Q' && key.isShiftDown) break
                        if (scrollOffset == 0) {
                            buffer.write(key.character.toString())
                            render(screen)
                        }
                    }
                    KeyType.Enter -> {
                        if (scrollOffset == 0) {
                            buffer.insertLineAtBottom()
                            render(screen)
                        }
                    }
                    KeyType.Backspace -> {
                        if (scrollOffset == 0) {
                            if (buffer.getCursor().col > 0) {
                                buffer.moveCursorLeft()
                                buffer.delete()
                                render(screen)
                            }
                        }
                    }
                    KeyType.Delete -> {
                        if (scrollOffset == 0) {
                            buffer.delete()
                            render(screen)
                        }
                    }
                    KeyType.ArrowUp -> {
                        if (key.isShiftDown) {
                            if (scrollOffset < buffer.getScrollbackSize()) {
                                scrollOffset++
                                render(screen)
                            }
                        } else if (scrollOffset == 0) {
                            buffer.moveCursorUp()
                            render(screen)
                        }
                    }
                    KeyType.ArrowDown -> {
                        if (key.isShiftDown) {
                            if (scrollOffset > 0) {
                                scrollOffset--
                                render(screen)
                            }
                        } else if (scrollOffset == 0) {
                            buffer.moveCursorDown()
                            render(screen)
                        }
                    }
                    KeyType.ArrowLeft -> {
                        if (scrollOffset == 0) {
                            buffer.moveCursorLeft()
                            render(screen)
                        }
                    }
                    KeyType.ArrowRight -> {
                        if (scrollOffset == 0) {
                            buffer.moveCursorRight()
                            render(screen)
                        }
                    }
                    KeyType.Home -> {
                        scrollOffset = buffer.getScrollbackSize()
                        render(screen)
                    }
                    KeyType.End -> {
                        scrollOffset = 0
                        render(screen)
                    }
                    else -> {}
                }
            }
        } finally {
            screen.stopScreen()
        }
    }

    private fun render(screen: TerminalScreen) {
        val graphics = screen.newTextGraphics()
        val termWidth = screen.terminalSize.columns
        val termHeight = screen.terminalSize.rows

        screen.clear()

        val viewportHeight = minOf(buffer.height, maxOf(1, termHeight - STATUS_BAR_HEIGHT))
        val viewportWidth = minOf(buffer.width, termWidth)

        val scrollbackSize = buffer.getScrollbackSize()
        val totalRows = scrollbackSize + buffer.height
        val viewEnd = totalRows - scrollOffset
        val viewStart = maxOf(0, viewEnd - viewportHeight)

        val cursorPos = buffer.getCursor()

        for (viewRow in 0..<minOf(viewportHeight, viewEnd - viewStart)) {
            val absoluteRow = viewStart + viewRow
            for (col in 0..<viewportWidth) {
                val cell = buffer.getAbsoluteCell(col, absoluteRow)
                if (cell.isWideContinuation) continue

                val fg = resolveColor(cell.attributes.foreground, isForeground = true)
                val bg = resolveColor(cell.attributes.background, isForeground = false)
                val sgrs = mapStyles(cell.attributes.styles)

                val isScreenRow = absoluteRow >= scrollbackSize
                val screenRow = absoluteRow - scrollbackSize
                val isCursor = scrollOffset == 0 &&
                        isScreenRow &&
                        screenRow == cursorPos.row &&
                        col == cursorPos.col

                if (isCursor) {
                    // Inverted: swap fg/bg for cursor visibility
                    graphics.foregroundColor = bg
                    graphics.backgroundColor = fg
                } else {
                    graphics.foregroundColor = fg
                    graphics.backgroundColor = bg
                }

                val ch = if (cell.char == '\u0000') ' ' else cell.char
                if (sgrs.isNotEmpty()) graphics.enableModifiers(*sgrs.toTypedArray())
                graphics.setCharacter(col, viewRow, ch)
                if (sgrs.isNotEmpty()) graphics.clearModifiers()
            }
        }

        // Border
        val borderRow = viewportHeight
        if (borderRow < termHeight) {
            graphics.foregroundColor = TextColor.ANSI.WHITE
            graphics.backgroundColor = TextColor.ANSI.DEFAULT
            graphics.putString(0, borderRow, "\u2500".repeat(minOf(viewportWidth, termWidth)))
        }

        // Status bar
        val statusRow = viewportHeight + 1
        if (statusRow < termHeight) {
            val viewingStr = if (scrollOffset > 0) "SCROLLBACK(-$scrollOffset)" else "SCREEN"
            val status = " ${buffer.width}x${buffer.height} " +
                    "Cur:(${cursorPos.col},${cursorPos.row}) " +
                    "SB:$scrollbackSize " +
                    "$viewingStr " +
                    "| Type/Del/Bksp=edit Shift+\u2191\u2193=scroll Shift+Q=quit"
            graphics.foregroundColor = TextColor.ANSI.BLACK
            graphics.backgroundColor = TextColor.ANSI.WHITE
            graphics.putString(0, statusRow, status.padEnd(termWidth).take(termWidth))
        }

        screen.refresh()
    }

    companion object {
        private const val STATUS_BAR_HEIGHT = 2

        /**
         * Resolves DEFAULT colors to concrete values so cursor inversion is visible.
         * DEFAULT foreground → WHITE, DEFAULT background → BLACK.
         */
        fun resolveColor(color: Color, isForeground: Boolean): TextColor.ANSI {
            if (color == Color.DEFAULT) {
                return if (isForeground) TextColor.ANSI.WHITE else TextColor.ANSI.BLACK
            }
            return mapColor(color)
        }

        fun mapColor(color: Color): TextColor.ANSI = when (color) {
            Color.DEFAULT -> TextColor.ANSI.DEFAULT
            Color.BLACK -> TextColor.ANSI.BLACK
            Color.RED -> TextColor.ANSI.RED
            Color.GREEN -> TextColor.ANSI.GREEN
            Color.YELLOW -> TextColor.ANSI.YELLOW
            Color.BLUE -> TextColor.ANSI.BLUE
            Color.MAGENTA -> TextColor.ANSI.MAGENTA
            Color.CYAN -> TextColor.ANSI.CYAN
            Color.WHITE -> TextColor.ANSI.WHITE
            Color.BRIGHT_BLACK -> TextColor.ANSI.BLACK_BRIGHT
            Color.BRIGHT_RED -> TextColor.ANSI.RED_BRIGHT
            Color.BRIGHT_GREEN -> TextColor.ANSI.GREEN_BRIGHT
            Color.BRIGHT_YELLOW -> TextColor.ANSI.YELLOW_BRIGHT
            Color.BRIGHT_BLUE -> TextColor.ANSI.BLUE_BRIGHT
            Color.BRIGHT_MAGENTA -> TextColor.ANSI.MAGENTA_BRIGHT
            Color.BRIGHT_CYAN -> TextColor.ANSI.CYAN_BRIGHT
            Color.BRIGHT_WHITE -> TextColor.ANSI.WHITE_BRIGHT
        }

        fun mapStyles(styles: Set<StyleFlag>): List<SGR> {
            val sgrs = mutableListOf<SGR>()
            for (style in styles) {
                when (style) {
                    StyleFlag.BOLD -> sgrs.add(SGR.BOLD)
                    StyleFlag.ITALIC -> sgrs.add(SGR.ITALIC)
                    StyleFlag.UNDERLINE -> sgrs.add(SGR.UNDERLINE)
                    StyleFlag.DIM -> {}
                    StyleFlag.STRIKETHROUGH -> sgrs.add(SGR.CROSSED_OUT)
                    StyleFlag.BLINK -> sgrs.add(SGR.BLINK)
                }
            }
            return sgrs
        }
    }
}
