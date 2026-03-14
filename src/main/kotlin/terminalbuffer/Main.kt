package terminalbuffer

import terminalbuffer.model.*

fun main() {
    val buf = TerminalBuffer(width = 40, height = 5, maxScrollback = 100)

    println("=== Terminal Buffer Demo ===")
    println()

    // Write some text
    buf.setForeground(Color.GREEN)
    buf.addStyle(StyleFlag.BOLD)
    buf.write("Hello, Terminal!")
    buf.resetAttributes()

    println("After writing 'Hello, Terminal!' in green bold:")
    println(buf.getScreenContent())
    println("Cursor: ${buf.getCursor()}")
    println()

    // Move to next line and write more
    buf.setCursor(0, 1)
    buf.setForeground(Color.CYAN)
    buf.write("Line 2: some content here")
    buf.resetAttributes()

    println("After writing line 2:")
    println(buf.getScreenContent())
    println()

    // Insert text mid-line
    buf.setCursor(8, 1)
    buf.setForeground(Color.YELLOW)
    buf.insert("INSERTED ")
    buf.resetAttributes()

    println("After inserting 'INSERTED ' at column 8, line 1:")
    println(buf.getScreenContent())
    println()

    // Fill a line
    buf.setCursor(0, 3)
    buf.setForeground(Color.RED)
    buf.fillLine('-')
    buf.resetAttributes()

    println("After filling line 3 with '-':")
    println(buf.getScreenContent())
    println()

    // Scroll test: write enough to cause scrolling
    for (i in 0 until 7) {
        buf.setCursor(0, buf.height - 1)
        buf.insertLineAtBottom()
        buf.setCursor(0, buf.height - 1)
        buf.write("Scroll line $i")
    }

    println("After scrolling (7 insertLineAtBottom calls):")
    println("Screen:")
    println(buf.getScreenContent())
    println()
    println("Scrollback size: ${buf.getScrollbackSize()}")
    println("Full content:")
    println(buf.getFullContent())
    println()

    // Wide character demo
    buf.clearScreen()
    buf.write("Wide: \u4F60\u597D\u4E16\u754C")  // 你好世界
    println("After writing wide chars (你好世界):")
    println(buf.getScreenContent())
    println("Cursor: ${buf.getCursor()}")

    // Check cell properties
    val cell = buf.getCell(6, 0)
    println("Cell at (6,0): char='${cell.char}', isWide=${cell.isWide}")
    val cont = buf.getCell(7, 0)
    println("Cell at (7,0): char='${cont.char}', isWideContinuation=${cont.isWideContinuation}")
    println()

    // Resize demo
    println("Before resize (40x5):")
    buf.clearAll()
    buf.write("AAAAAAAAAA")
    buf.setCursor(0, 1)
    buf.write("BBBBBBBBBB")
    println(buf.getScreenContent())
    println()

    buf.resize(20, 3)
    println("After resize to 20x3:")
    println(buf.getScreenContent())
    println("Scrollback size: ${buf.getScrollbackSize()}")

    println()
    println("=== Demo Complete ===")
}
