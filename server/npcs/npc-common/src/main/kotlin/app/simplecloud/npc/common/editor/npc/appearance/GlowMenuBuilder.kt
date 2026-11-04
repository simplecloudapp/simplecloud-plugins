package app.simplecloud.npc.common.editor.npc.appearance

import app.simplecloud.npc.common.editor.core.back
import app.simplecloud.npc.common.editor.menu.EditorMenu
import app.simplecloud.npc.common.editor.menu.Pane
import app.simplecloud.npc.common.editor.npc.NpcEditorContext
import app.simplecloud.npc.common.editor.npc.NpcFormat
import app.simplecloud.npc.common.editor.npc.Refresh
import app.simplecloud.npc.common.editor.ui.Layout
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.core.config.NpcConfig
import app.simplecloud.npc.core.platform.NpcPlayer

object GlowMenuBuilder {
    private const val SIZE = Layout.SIZE

    private val COLOR_SLOTS = Layout.row(1, 7) + Layout.row(2, 7) + Layout.row(3, 2)

    fun build(context: NpcEditorContext, player: NpcPlayer, config: NpcConfig): EditorMenu {
        val current = NpcFormat.glowColor(config.entity)
        val pane = Pane(SIZE)

        NpcFormat.GLOW_COLORS.zip(COLOR_SLOTS).forEach { (color, slot) ->
            val selected = color.teamName.equals(current, true)
            pane.left(slot, Ui.option(color.icon, color.label, listOf(""), selected)) {
                context.commitAndBack(player, config.id, refresh = Refresh.ENTITY) { fresh ->
                    fresh.copy(entity = fresh.entity.copy(glowing = true, glowColor = color.teamName))
                }
            }
        }

        pane.back(context, player)

        return pane.menu(Ui.title("Glow Color", NpcFormat.displayName(config)))
    }
}
