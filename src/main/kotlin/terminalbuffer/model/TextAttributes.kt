package terminalbuffer.model

data class TextAttributes(
    val foreground: Color = Color.DEFAULT,
    val background: Color = Color.DEFAULT,
    val styles: Set<StyleFlag> = emptySet()
)
