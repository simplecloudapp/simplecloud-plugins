package app.simplecloud.plugin.core.shared.command

/** The messages of a help list. Descriptions are keyed by the usage without the root command. */
interface HelpMessages {
    val entry: String
    val hover: String
    val descriptions: Map<String, String>

    companion object {
        const val ENTRY = "   <interactive><subcommand></interactive> <muted><arguments>"
        const val HOVER = "<highlight><description><br><muted>Usage: <highlight><usage><br><br><interactive>Click to type this command"
    }
}
