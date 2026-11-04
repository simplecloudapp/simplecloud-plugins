package app.simplecloud.npc.common.editor.inventory.screens

import app.simplecloud.npc.common.editor.inventory.InventoryEditorContext
import app.simplecloud.npc.common.editor.inventory.InventoryEditorScreen
import app.simplecloud.npc.common.editor.inventory.MenuTemplate
import app.simplecloud.npc.common.editor.menu.EditorMenu
import app.simplecloud.npc.common.editor.menu.Pane
import app.simplecloud.npc.common.editor.ui.Layout
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.text.sendError
import app.simplecloud.npc.common.text.sendSuccess
import app.simplecloud.npc.core.config.ConfigIds
import app.simplecloud.npc.core.platform.NpcPlayer

object TemplatesMenuBuilder {
    private const val SIZE = Layout.SIZE
    private val TEMPLATE_SLOTS = Layout.row(1, 5)

    fun build(context: InventoryEditorContext, player: NpcPlayer): EditorMenu {
        val pane = Pane(SIZE)

        MenuTemplate.entries.zip(TEMPLATE_SLOTS).forEach { (template, slot) ->
            pane.left(
                slot,
                Ui.item(
                    template.icon,
                    "<ttl>${template.label}",
                    template.description.map { "<bd>$it" } + listOf("", "<key>Left-click to create it"),
                ),
            ) { create(context, player, template) }
        }

        return pane.menu("New Menu")
    }

    private fun create(context: InventoryEditorContext, player: NpcPlayer, template: MenuTemplate) {
        val id = freeId(context, template)
        if (!ConfigIds.isValid(id)) return player.sendError("Could not find a free id for a new menu.")

        context.inventoryRepository.save(template.build(id))
        player.sendSuccess("Created menu {} from the {} template.", id, template.label)
        context.reset(player, listOf(InventoryEditorScreen.Hub(id), InventoryEditorScreen.Canvas(id)))
    }

    private fun freeId(context: InventoryEditorContext, template: MenuTemplate): String {
        val base = template.name.lowercase().replace('_', '-')
        return generateSequence(1) { it + 1 }
            .map { if (it == 1) base else "$base-$it" }
            .first { context.inventoryRepository.find(it) == null }
    }
}
