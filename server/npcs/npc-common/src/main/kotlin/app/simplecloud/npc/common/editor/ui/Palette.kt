package app.simplecloud.npc.common.editor.ui

object Palette {

    private val TOKENS = listOf(
        "<ttl>" to "<brand>",
        "<bd>" to "<muted>",
        "<hnt>" to "<separator>",
        "<val>" to "<highlight>",
        "<key>" to "<interactive>",
        "<on>" to "<success>",
        "<off>" to "<muted>",
        "<warn>" to "<warning>",
        "<err>" to "<error>",
    )

    fun expand(text: String): String {
        if (text.isEmpty() || '<' !in text) return text
        return TOKENS.fold(text) { expanded, (token, hex) -> expanded.replace(token, hex) }
    }
}
