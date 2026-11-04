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
        val glowing = config.entity.glowing
        val current = NpcFormat.glowColor(config.entity)
        val pane = Pane(SIZE)
        val currentLabel = NpcFormat.GLOW_COLORS.firstOrNull { it.teamName.equals(current, true) }?.label ?: current
        pane[Layout.HEADER] = Ui.item(
            "GLOWSTONE_DUST",
            "<ttl>Which Glow Color?",
            listOf(
                "<bd>The outline players see",
                "<bd>around the NPC.",
                "",
                if (glowing) "<bd>Now: <val>$currentLabel" else "<bd>Now: <off>Glow is off",
                if (glowing) null else "<hnt>Picking a color turns it on.",
            ).filterNotNull(),
        )

        NpcFormat.GLOW_COLORS.zip(COLOR_SLOTS).forEach { (color, slot) ->
            val selected = glowing && color.teamName.equals(current, true)
            pane.left(slot, Ui.option(color.icon, color.label, emptyList(), selected, nameColor(color.teamName))) {
                context.commitAndBack(player, config.id, refresh = Refresh.ENTITY) { fresh ->
                    fresh.copy(entity = fresh.entity.copy(glowing = true, glowColor = color.teamName))
                }
            }
        }

        pane.back(context, player)

        return pane.menu(Ui.title("Glow Color", NpcFormat.displayName(config)))
    }

    private fun nameColor(teamName: String): String = when (teamName) {
        "black" -> "<gray>"
        "dark_blue" -> "<blue>"
        "dark_purple" -> "<light_purple>"
        else -> "<$teamName>"
    }
}
