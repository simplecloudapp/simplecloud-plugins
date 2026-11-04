package app.simplecloud.npc.common.command

/** A line of a help page: a command without the root, or a plain note. */
sealed interface HelpLine {
    data class Command(val usage: String, val description: String, val details: String = "") : HelpLine

    data class Note(val text: String) : HelpLine
}
