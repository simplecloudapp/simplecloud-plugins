package app.simplecloud.plugin.core.shared.command

/** Widths of the default Minecraft font in chat pixels, used to line up columns. */
object ChatWidth {

    const val SPACE = 4

    fun of(text: String): Int = text.sumOf { char ->
        when (char) {
            'i', '!', '.', ',', ':', ';', '|', '\'' -> 2
            'l', '`' -> 3
            't', 'I', ' ', '[', ']' -> 4
            'f', 'k', '<', '>', '(', ')', '"', '*' -> 5
            '@', '~' -> 7
            else -> 6
        }
    }

    fun spaces(width: Int): String = " ".repeat(((width + SPACE - 1) / SPACE).coerceAtLeast(1))
}
