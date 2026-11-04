package app.simplecloud.npc.common.editor.inventory.screens

import app.simplecloud.npc.common.editor.Prompt
import app.simplecloud.npc.common.editor.PromptFormat
import app.simplecloud.npc.common.editor.PromptResult
import app.simplecloud.npc.common.editor.PromptTokens
import app.simplecloud.npc.common.editor.core.back
import app.simplecloud.npc.common.editor.core.toggle
import app.simplecloud.npc.common.editor.inventory.InventoryEditorContext
import app.simplecloud.npc.common.editor.inventory.InventoryEditorScreen
import app.simplecloud.npc.common.editor.inventory.InventoryPickerPurpose
import app.simplecloud.npc.common.editor.inventory.LiveGroups
import app.simplecloud.npc.common.editor.menu.EditorMenu
import app.simplecloud.npc.common.editor.menu.MenuClick
import app.simplecloud.npc.common.editor.menu.Pane
import app.simplecloud.npc.common.editor.ui.Layout
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.item.NpcItem
import app.simplecloud.npc.core.inventory.InventoryConfiguration
import app.simplecloud.npc.core.inventory.InventoryConfiguration.StateLook
import app.simplecloud.npc.core.platform.NpcPlayer

object StateLookMenuBuilder {
    private const val MATERIAL_SLOT = 19
    private const val NAME_SLOT = 21
    private const val LORE_SLOT = 23
    private const val GLINT_SLOT = 25
    private const val RESET_SLOT = Layout.TOOL_RIGHT
    private const val PICK = "<key>Left-click to pick one"
    private const val TYPE = "<key>Left-click to type it in chat"

    fun build(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        screen: InventoryEditorScreen.StateLook,
    ): EditorMenu {
        val group = screen.group
        val state = screen.state
        val stateName = StateLooksMenuBuilder.label(state)
        val look = LiveGroups.source(config, group)?.lookFor(state)
        val template = StateLooksMenuBuilder.template(config, group)
        val pane = Pane(Layout.SIZE)

        fun change(transform: (StateLook) -> StateLook?) {
            context.change(player, config.id) { LiveGroups.setLook(it, group, state, transform) }
            context.render(player, screen)
        }

        fun prompt(title: String, instruction: String, current: String?, apply: (String) -> Unit) {
            context.chatPrompt(
                player,
                Prompt(
                    title = title,
                    instruction = instruction,
                    format = PromptFormat.MINI_MESSAGE,
                    placeholders = PromptTokens.MENU + PromptTokens.ENTRY,
                    current = current,
                    onCancel = { context.render(player, screen) },
                    onSubmit = { input ->
                        val text = input.trim()
                        val rejected = PromptResult.rejectInvalidMiniMessage(text)
                        if (rejected != null) return@Prompt rejected
                        apply(text)
                        PromptResult.Accepted
                    },
                ),
            )
        }

        pane[Layout.HEADER] = StateLooksMenuBuilder.preview(template, state, look)

        val material = look?.material
        val materialLine = if (material == null) null else "<bd>Now: <val>${Ui.pretty(material)}"
        field(pane, MATERIAL_SLOT, Ui.field(material ?: template.material, "Material", materialLine, PICK), {
            context.navigate(player, InventoryEditorScreen.Picker(config.id, InventoryPickerPurpose.LookMaterial(group, state)))
        }) { change { it.copy(material = null) } }

        val name = look?.name
        val nameLine = if (name == null) null else "<bd>Now: <val>${Ui.quote(name)}"
        field(pane, NAME_SLOT, Ui.field("NAME_TAG", "Name", nameLine, TYPE), {
            prompt("Look Name", "Type the name entries show while ${stateName.lowercase()}.", name ?: template.name) { text ->
                change { it.copy(name = text) }
            }
        }) { change { it.copy(name = null) } }

        val lore = look?.lore
        val loreLine = if (lore == null) null else "<bd>Lines: <val>${lore.size}"
        field(pane, LORE_SLOT, Ui.field("WRITABLE_BOOK", "Lore", loreLine, TYPE), {
            prompt("Look Lore", "Type the lore in chat, split lines with |.", (lore ?: template.lore).joinToString(" | ")) { text ->
                change { it.copy(lore = text.split('|').map(String::trim).toMutableList()) }
            }
        }) { change { it.copy(lore = null) } }

        val glowing = look?.glowing ?: template.glowing
        pane.toggle(GLINT_SLOT, "Enchant Glint", glowing, material = "EXPERIENCE_BOTTLE") {
            change { it.copy(glowing = !glowing) }
        }

        if (look != null) {
            pane.left(
                RESET_SLOT,
                Ui.item(
                    "BARRIER",
                    "<err>Reset Look",
                    listOf("<bd>Entries look the same", "<bd>as the slot again.", "", "<key>Left-click to reset it"),
                ),
            ) { change { null } }
        }

        pane.back(context, player)

        return pane.menu(Ui.title("$stateName Look", config.id))
    }

    private fun field(pane: Pane, slot: Int, item: NpcItem, edit: () -> Unit, clear: () -> Unit) {
        pane.on(slot, item) { click ->
            when (click) {
                MenuClick.LEFT -> edit()
                MenuClick.DROP -> if (item.glowing) clear()
                else -> Unit
            }
        }
    }
}
