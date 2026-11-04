package app.simplecloud.npc.common.editor.core

import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.item.NpcItem

/** The top of a picker: what is being picked, what it is for and what is picked now. */
class PickerHeader(private val icon: String, private val title: String, private val lore: List<String>) {
    fun item(): NpcItem = Ui.item(icon, "<ttl>$title", lore)

    companion object {
        fun now(value: String?): String = if (value == null) "<bd>Now: <off>Nothing" else "<bd>Now: <val>$value"
    }
}
