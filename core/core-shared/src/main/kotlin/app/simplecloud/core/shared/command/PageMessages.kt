package app.simplecloud.core.shared.command

interface PageMessages {
    val footer: String
    val previous: String
    val next: String

    companion object {
        const val FOOTER = "<previous><info>Page <page>/<pages><next>"
        const val PREVIOUS = "<interactive>Previous</interactive>  "
        const val NEXT = "  <interactive>Next"
    }
}
