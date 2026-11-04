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

    /** The fixed tool slots of the bottom row: find, scope or sort left of the middle, delete or reset right of it. */
    const val TOOL_LEFT = 48
    const val TOOL_MIDDLE = 49
    const val TOOL_RIGHT = 50

    private const val COLUMNS = 9
    private const val MIDDLE = 4

    /**
     * Slots for a list of [count] entries in [rows] content rows from [firstRow] (1 to 3) on: rows of seven, every row
     * centered, so a short list sits in the middle like single settings do.
     */
    fun list(count: Int, firstRow: Int = 1, rows: Int = 3): List<Int> {
        val capped = count.coerceIn(0, rows * ROW_SLOTS)
        return (0 until rows).flatMap { offset ->
            val inRow = (capped - offset * ROW_SLOTS).coerceIn(0, ROW_SLOTS)
            val start = (firstRow + offset + 1) * COLUMNS
            centered(inRow).map { start + it }
        }
    }

    /** Columns for [count] entries side by side, centered; an even count leaves the middle column empty. */
    private fun centered(count: Int): List<Int> {
        if (count % 2 == 1) {
            val first = (ROW_SLOTS - count) / 2 + 1
            return (first until first + count).toList()
        }
        val half = count / 2
        return ((MIDDLE - half) until MIDDLE) + ((MIDDLE + 1)..(MIDDLE + half))
    }

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
