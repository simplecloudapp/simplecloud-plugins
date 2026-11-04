package app.simplecloud.npc.common.editor.npc.tabs

import app.simplecloud.npc.common.editor.menu.EditorMenu
import app.simplecloud.npc.common.editor.menu.Pane
import app.simplecloud.npc.common.editor.npc.NpcEditorContext
import app.simplecloud.npc.common.editor.npc.NpcEditorScreen
import app.simplecloud.npc.common.editor.npc.NpcFormat
import app.simplecloud.npc.common.editor.npc.NpcTab
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.core.config.NpcConfig
import app.simplecloud.npc.core.platform.NpcPlayer

object TabFrame {
    const val SIZE = 54

    private val TAB_SLOTS = listOf(1, 2, 3, 5, 6, 7)

    fun pane(context: NpcEditorContext, player: NpcPlayer, config: NpcConfig, active: NpcTab): Pane {
        val pane = Pane(SIZE)

        NpcTab.entries.forEachIndexed { index, tab ->
            val slot = TAB_SLOTS[index]
            val selected = tab == active
            val name = if (selected) "<val>${tab.label}" else "<ttl>${tab.label}"
            val lore = listOf("<bd>${tab.description}") + if (selected) emptyList() else listOf("", "<key>Left-click to open")
            val item = if (tab == NpcTab.NPC) {
                Ui.head(name, lore, config.entity.skin.texture, config.entity.skin.signature, glowing = selected)
            } else {
                Ui.item(tab.material, name, lore, glowing = selected)
            }

            if (selected) {
                pane[slot] = item
            } else {
                pane.left(slot, item) { context.replace(player, NpcEditorScreen.Tab(config.id, tab)) }
            }
        }

        return pane
    }

    fun menu(pane: Pane, config: NpcConfig, tab: NpcTab): EditorMenu = pane.menu(Ui.title(tab.label, NpcFormat.displayName(config)))
}
