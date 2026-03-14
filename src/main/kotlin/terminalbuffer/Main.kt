package terminalbuffer

import terminalbuffer.model.*
import terminalbuffer.ui.TerminalBufferViewer

fun main() {
    val buf = TerminalBuffer(width = 100, height = 24, maxScrollback = 200)

    // ── Title bar ───────────────────────────────────────────────
    buf.setBackground(Color.BLUE)
    buf.setForeground(Color.WHITE)
    buf.addStyle(StyleFlag.BOLD)
    buf.fillLine(' ')
    buf.setCursor(2, 0)
    buf.write(" Terminal Buffer Demo ")
    buf.resetAttributes()

    // ── Colorful text samples ───────────────────────────────────
    buf.setCursor(0, 2)
    buf.setForeground(Color.GREEN)
    buf.addStyle(StyleFlag.BOLD)
    buf.write("$ ")
    buf.resetAttributes()
    buf.setForeground(Color.WHITE)
    buf.write("echo \"Hello from the terminal buffer!\"")
    buf.resetAttributes()

    buf.setCursor(0, 3)
    buf.setForeground(Color.CYAN)
    buf.write("Hello from the terminal buffer!")
    buf.resetAttributes()

    // ── Color palette ───────────────────────────────────────────
    buf.setCursor(0, 5)
    buf.setForeground(Color.WHITE)
    buf.addStyle(StyleFlag.UNDERLINE)
    buf.write("Standard colors:")
    buf.resetAttributes()

    buf.setCursor(0, 6)
    val standardColors = listOf(
        Color.BLACK, Color.RED, Color.GREEN, Color.YELLOW,
        Color.BLUE, Color.MAGENTA, Color.CYAN, Color.WHITE
    )
    for (color in standardColors) {
        buf.setBackground(color)
        buf.write("  ")
        buf.resetAttributes()
    }

    buf.setCursor(0, 7)
    buf.setForeground(Color.WHITE)
    buf.addStyle(StyleFlag.UNDERLINE)
    buf.write("Bright colors:")
    buf.resetAttributes()

    buf.setCursor(0, 8)
    val brightColors = listOf(
        Color.BRIGHT_BLACK, Color.BRIGHT_RED, Color.BRIGHT_GREEN, Color.BRIGHT_YELLOW,
        Color.BRIGHT_BLUE, Color.BRIGHT_MAGENTA, Color.BRIGHT_CYAN, Color.BRIGHT_WHITE
    )
    for (color in brightColors) {
        buf.setBackground(color)
        buf.write("  ")
        buf.resetAttributes()
    }

    // ── Style showcase ──────────────────────────────────────────
    buf.setCursor(0, 10)
    buf.setForeground(Color.WHITE)
    buf.addStyle(StyleFlag.UNDERLINE)
    buf.write("Text styles:")
    buf.resetAttributes()

    buf.setCursor(2, 11)
    buf.addStyle(StyleFlag.BOLD)
    buf.write("Bold")
    buf.resetAttributes()
    buf.write("  ")
    buf.addStyle(StyleFlag.ITALIC)
    buf.write("Italic")
    buf.resetAttributes()
    buf.write("  ")
    buf.addStyle(StyleFlag.UNDERLINE)
    buf.write("Underline")
    buf.resetAttributes()
    buf.write("  ")
    buf.addStyle(StyleFlag.DIM)
    buf.write("Dim")
    buf.resetAttributes()
    buf.write("  ")
    buf.addStyle(StyleFlag.STRIKETHROUGH)
    buf.write("Strikethrough")
    buf.resetAttributes()
    buf.write("  ")
    buf.addStyle(StyleFlag.BOLD)
    buf.addStyle(StyleFlag.ITALIC)
    buf.addStyle(StyleFlag.UNDERLINE)
    buf.setForeground(Color.YELLOW)
    buf.write("All combined")
    buf.resetAttributes()

    // ── Wide characters ─────────────────────────────────────────
    buf.setCursor(0, 13)
    buf.setForeground(Color.WHITE)
    buf.addStyle(StyleFlag.UNDERLINE)
    buf.write("Wide characters (CJK):")
    buf.resetAttributes()

    buf.setCursor(2, 14)
    buf.setForeground(Color.BRIGHT_RED)
    buf.write("\u4F60\u597D")  // 你好
    buf.setForeground(Color.BRIGHT_GREEN)
    buf.write("\u4E16\u754C")  // 世界
    buf.resetAttributes()
    buf.write(" = Hello World")

    // ── Simulated command output ────────────────────────────────
    buf.setCursor(0, 16)
    buf.setForeground(Color.WHITE)
    buf.addStyle(StyleFlag.UNDERLINE)
    buf.write("Simulated ls output:")
    buf.resetAttributes()

    val files = listOf(
        Triple("src/", Color.BLUE, true),
        Triple("build.gradle.kts", Color.GREEN, false),
        Triple("README.md", Color.WHITE, false),
        Triple("gradlew", Color.RED, true),
        Triple("settings.gradle.kts", Color.GREEN, false),
    )
    buf.setCursor(2, 17)
    for ((name, color, bold) in files) {
        buf.setForeground(color)
        if (bold) buf.addStyle(StyleFlag.BOLD)
        buf.write(name)
        buf.resetAttributes()
        buf.write("  ")
    }

    // ── Scrollback hint ─────────────────────────────────────────
    buf.setCursor(0, 19)
    buf.setForeground(Color.BRIGHT_BLACK)
    buf.write("(Scroll up with Shift+\u2191 to see scrollback history)")
    buf.resetAttributes()

    // Push current content into scrollback by inserting blank lines
    for (i in 1..18) buf.insertLineAtBottom()

    // Draw a prompt section at the bottom of the now-scrolled screen
    buf.setCursor(0, 20)
    buf.setForeground(Color.GREEN)
    buf.addStyle(StyleFlag.BOLD)
    buf.write("user@terminal")
    buf.resetAttributes()
    buf.write(":")
    buf.setForeground(Color.BLUE)
    buf.addStyle(StyleFlag.BOLD)
    buf.write("~/project")
    buf.resetAttributes()
    buf.write("$ ")
    buf.setForeground(Color.WHITE)
    buf.write("./gradlew build")
    buf.resetAttributes()

    buf.setCursor(0, 21)
    buf.setForeground(Color.WHITE)
    buf.write("> Task :compileKotlin")

    buf.setCursor(0, 22)
    buf.setForeground(Color.BRIGHT_GREEN)
    buf.addStyle(StyleFlag.BOLD)
    buf.write("BUILD SUCCESSFUL")
    buf.resetAttributes()
    buf.setForeground(Color.WHITE)
    buf.write(" in 3s")

    buf.setCursor(0, 23)
    buf.setForeground(Color.GREEN)
    buf.addStyle(StyleFlag.BOLD)
    buf.write("user@terminal")
    buf.resetAttributes()
    buf.write(":")
    buf.setForeground(Color.BLUE)
    buf.addStyle(StyleFlag.BOLD)
    buf.write("~/project")
    buf.resetAttributes()
    buf.write("$ ")
    buf.resetAttributes()

    // Launch the interactive viewer
    val viewer = TerminalBufferViewer(buf)
    viewer.show()
}
