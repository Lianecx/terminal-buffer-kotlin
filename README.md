# Terminal Text Buffer

A terminal text buffer implementation in Kotlin — the core data structure that terminal emulators use to store and manipulate displayed text. Includes an interactive TUI viewer built with Lanterna.

## Building and Running

Requires JDK 21.

```bash
./gradlew build   # compile
./gradlew run     # launch the interactive demo
```

## Architecture

### Data Model (`terminalbuffer.model`)

| Class | Description |
|---|---|
| `Cell` | A single character cell: character, attributes, wide char flags. Empty cells hold `' '` with default attributes. |
| `Color` | Enum with `DEFAULT` + 16 standard terminal colors (8 normal + 8 bright). |
| `StyleFlag` | Enum: `BOLD`, `ITALIC`, `UNDERLINE`, `DIM`, `STRIKETHROUGH`, `BLINK`. |
| `TextAttributes` | Immutable data class grouping foreground color, background color, and style flags. |
| `CursorPosition` | `(col, row)` pair. |

### Core Components (`terminalbuffer`)

| Class | Description |
|---|---|
| `BufferLine` | A fixed-width line of `Cell`s backed by an array. Supports get/set, fill, clear, insert, copy, and resize. |
| `Screen` | The visible portion of the terminal. Uses a **circular buffer** (`topRow` offset) for O(1) scroll operations instead of copying the entire array on each scroll. |
| `Scrollback` | Bounded FIFO of `BufferLine`s via `ArrayDeque`. O(1) add/evict. When capacity is reached, oldest lines are dropped. |
| `WideCharUtils` | Detects wide (double-width) characters: CJK ideographs, fullwidth forms, emoji. Uses `Character.isIdeographic()` plus explicit Unicode range checks. |
| `TerminalBuffer` | The main facade. Composes `Screen` + `Scrollback` and exposes all buffer operations. |

### Viewer (`terminalbuffer.ui`)

| Class | Description                                                                                                                                                                |
|---|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `TerminalBufferViewer` | Lanterna-based TUI that renders a `TerminalBuffer` with real terminal colors, styles, and a visible cursor. Supports keyboard editing, resizing and scrollback navigation. |

## TerminalBuffer API

### Setup

```kotlin
val buf = TerminalBuffer(
    width = 80,          // columns
    height = 24,         // rows
    maxScrollback = 1000 // max lines preserved above the screen
)
```

### Attributes

Set the current text attributes. These apply to all subsequent write/insert/fill operations.

```kotlin
buf.setForeground(Color.GREEN)
buf.setBackground(Color.BLUE)
buf.addStyle(StyleFlag.BOLD)
buf.removeStyle(StyleFlag.BOLD)
buf.setStyles(setOf(StyleFlag.ITALIC, StyleFlag.UNDERLINE))
buf.resetAttributes()       // back to defaults
buf.getAttributes()         // current TextAttributes
```

### Cursor

The cursor marks where the next character will be written. It is always clamped to screen bounds.

```kotlin
buf.getCursor()             // CursorPosition(col, row)
buf.setCursor(col, row)     // clamped to [0, width-1] x [0, height-1]
buf.moveCursorUp(n)
buf.moveCursorDown(n)
buf.moveCursorLeft(n)
buf.moveCursorRight(n)
```

### Editing (cursor + attribute dependent)

**Write** — overwrites cells at the cursor position. Advances the cursor. Wraps at the right edge; scrolls when wrapping past the bottom.

```kotlin
buf.write("Hello, world!")
```

**Insert** — inserts text at the cursor, shifting existing content to the right. Overflow wraps to the next line. Content that falls off the bottom is lost (scrolled to scrollback).

```kotlin
buf.insert("INSERTED ")
```

**Delete** — removes cells at the cursor, shifting remaining content left. Empty cells fill in from the right.

```kotlin
buf.delete(3) // delete 3 cells
```

**Fill line** — fills the entire cursor line with a character using current attributes. Cursor stays put.

```kotlin
buf.fillLine('-')
buf.fillLine()    // fills with spaces (clears the line visually)
```

### Editing (cursor/attribute independent)

```kotlin
buf.insertLineAtBottom()  // scroll up by 1, top line -> scrollback, blank bottom line
buf.clearScreen()         // empty all screen cells, cursor -> (0,0), scrollback preserved
buf.clearAll()            // clear screen + scrollback, cursor -> (0,0)
```

### Content Access

**Screen access** — row 0 is the top of the screen:

```kotlin
buf.getCell(col, row)          // Cell at screen position
buf.getChar(col, row)          // character only
buf.getAttributesAt(col, row)  // TextAttributes only
buf.getScreenLine(row)         // line as trimmed string
buf.getScreenContent()         // all screen lines joined by \n
```

**Absolute access** — row 0 is the oldest scrollback line, screen follows after:

```kotlin
buf.getScrollbackSize()
buf.getAbsoluteCell(col, absoluteRow)
buf.getAbsoluteChar(col, absoluteRow)
buf.getAbsoluteAttributesAt(col, absoluteRow)
buf.getAbsoluteLine(absoluteRow)
buf.getFullContent()           // scrollback + screen joined by \n
```

**Scrollback-only access:**

```kotlin
buf.getScrollbackLine(row)     // row 0 = oldest
```

### Wide Characters

CJK ideographs, fullwidth forms, and emoji automatically occupy 2 cells. The first cell has `isWide = true`, the second has `isWideContinuation = true`. Overwriting either half of a wide character clears the other to prevent orphaned halves. A wide character that doesn't fit at the last column wraps to the next line.

```kotlin
buf.write("\u4F60\u597D")  // 你好 — occupies 4 cells
```

### Resize

```kotlin
buf.resize(newWidth, newHeight)
```

- **Height shrinks**: top screen lines move to scrollback.
- **Height grows**: lines are pulled back from scrollback if available, otherwise blank.
- **Width changes**: lines are truncated or padded.
- Cursor is clamped to new bounds.

## Possible Improvements

- **Soft-wrap tracking** — recording which line breaks were caused by wrapping vs explicit newlines would enable smarter resize re-flow
- **Alternate screen buffer** — real terminals support switching between primary and alternate buffers (used by vim, less, etc.)
- **Scroll regions** — defining a subset of rows that scroll independently (used by status bars, split panes)
- **Selection and copy** — selecting text ranges and extracting them as strings

## Note on AI Usage

AI (Claude Code) was used during development, significantly speeding up the implementation process. All code was carefully reviewed by me to ensure it meets my own code standards and style.
