package app.simplecloud.npc.common.editor.menu

import app.simplecloud.npc.common.editor.ui.Layout
import app.simplecloud.npc.common.item.NpcItem

class Pane(val size: Int) {

    private val elements = mutableMapOf<Int, Element>()
    private var fallback: ((slot: Int, click: MenuClick) -> Unit)? = null

    val navRow: IntRange get() = (size - 9) until size

    operator fun set(slot: Int, item: NpcItem) {
        elements[slot] = Element(item)
    }

    operator fun set(slot: Int, element: Element) {
        elements[slot] = element
    }

    fun on(slot: Int, item: NpcItem, onClick: (MenuClick) -> Unit) {
        elements[slot] = Element(item, onClick)
    }

    fun on(slot: Int, item: NpcItem, click: MenuClick, action: () -> Unit) {
        elements[slot] = Element(item) { if (it == click) action() }
    }

    fun left(slot: Int, item: NpcItem, action: () -> Unit) = on(slot, item, MenuClick.LEFT, action)

    fun otherwise(handler: (slot: Int, click: MenuClick) -> Unit) {
        fallback = handler
    }

    fun menu(title: String): EditorMenu = EditorMenu(title, size, placeTools(), fallback)

    private fun placeTools(): Map<Int, Element> {
        val tools = elements.filterKeys { it < 0 }.toSortedMap(compareByDescending { it }).values.toList()
        val placed = elements.filterKeys { it >= 0 }.toMutableMap()
        if (tools.isEmpty() || size != Layout.SIZE) return placed

        val middleTaken = (navRow.first + MIDDLE_COLUMN) in placed
        val slots = Layout.toolSlots(tools.size, middleTaken)
        val onlyRight = elements.keys.filter { it < 0 } == listOf(Layout.TOOL_RIGHT)
        val shifted = if (onlyRight) listOf(navRow.first + MIDDLE_COLUMN + 1) else slots
        shifted.zip(tools).forEach { (slot, tool) -> placed[slot] = tool }
        return placed
    }

    companion object {
        const val GRAY_PANE = "GRAY_STAINED_GLASS_PANE"
        private const val MIDDLE_COLUMN = 4
    }
}
