package terminalbuffer.model

data class Cell(
    val char: Char = ' ',
    val attributes: TextAttributes = TextAttributes(),
    val isWide: Boolean = false,
    val isWideContinuation: Boolean = false
)

val EMPTY_CELL = Cell()
