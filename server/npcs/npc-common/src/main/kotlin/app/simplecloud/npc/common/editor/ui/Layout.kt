package app.simplecloud.npc.common.editor.ui

/**
 * The one layout every editor menu uses: six rows, the edited thing alone in the middle of the top row, content in
 * the inner seven columns of rows 3 to 5, and the control row at the bottom (Back or Previous, tools, page count,
 * Next).
 */
object Layout {
    const val SIZE = 54

    const val HEADER = 4

    /** Every content slot, row by row. */
    val CONTENT: List<Int> = (19..25) + (28..34) + (37..43)

    const val ROW_SLOTS = 7

    const val TOOL_LEFT = 47
    const val TOOL_RIGHT = 51

    private const val COLUMNS = 9

    /**
     * [count] slots of content row [row] (1 to 3), centered: up to four items with a gap between them, five to seven
     * side by side.
     */
    fun row(row: Int, count: Int): List<Int> {
        val start = (row + 1) * COLUMNS
        val columns = when (count) {
            0 -> emptyList()
            1 -> listOf(4)
            2 -> listOf(3, 5)
            3 -> listOf(2, 4, 6)
            4 -> listOf(1, 3, 5, 7)
            5 -> (2..6).toList()
            6 -> listOf(1, 2, 3, 5, 6, 7)
            else -> (1..7).toList()
        }

        return columns.map { start + it }
    }
}
