package app.simplecloud.npc.common.editor.inventory.screens

import app.simplecloud.npc.common.editor.Prompt
import app.simplecloud.npc.common.editor.PromptFormat
import app.simplecloud.npc.common.editor.PromptResult
import app.simplecloud.npc.common.editor.PromptTokens
import app.simplecloud.npc.common.editor.core.back
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
        val materialItem = lookField(
            material ?: template.material,
            "Material",
            shown(material, Ui::pretty),
            Ui.pretty(template.material),
            PICK,
        )
        field(pane, MATERIAL_SLOT, materialItem, material != null, {
            context.navigate(player, InventoryEditorScreen.Picker(config.id, InventoryPickerPurpose.LookMaterial(group, state)))
        }) { change { it.copy(material = null) } }

        val name = look?.name
        val templateName = template.name
        val nameItem = lookField(
            "NAME_TAG",
            "Name",
            shown(name, Ui::quote),
            if (templateName == null) "Default" else Ui.quote(templateName),
            TYPE,
        )
        field(pane, NAME_SLOT, nameItem, name != null, {
            prompt("Look Name", "Type the name entries show while ${stateName.lowercase()}.", name ?: template.name) { text ->
                change { it.copy(name = text) }
            }
        }) { change { it.copy(name = null) } }

        val lore = look?.lore
        val loreItem = lookField(
            "WRITABLE_BOOK",
            "Lore",
            shown(lore) { "${it.size} lines" },
            "${template.lore.size} lines",
            TYPE,
        )
        field(pane, LORE_SLOT, loreItem, lore != null, {
            prompt("Look Lore", "Type the lore in chat, split lines with |.", (lore ?: template.lore).joinToString(" | ")) { text ->
                change { it.copy(lore = text.split('|').map(String::trim).toMutableList()) }
            }
        }) { change { it.copy(lore = null) } }

        val glint = look?.glowing
        val glowing = glint ?: template.glowing
        val glintItem = lookField(
            "EXPERIENCE_BOTTLE",
            "Enchant Glint",
            shown(glint, ::onOff),
            onOff(template.glowing),
            if (glowing) "<key>Left-click to turn it off" else "<key>Left-click to turn it on",
        )
        field(pane, GLINT_SLOT, glintItem, glint != null, { change { it.copy(glowing = !glowing) } }) {
            change { it.copy(glowing = null) }
        }

        if (look != null) {
            pane.left(
                RESET_SLOT,
                Ui.item(
                    "RED_CONCRETE",
                    "<err>Reset Look",
                    listOf("<bd>Entries look the same", "<bd>as the slot again.", "", "<key>Left-click to reset it"),
                ),
            ) { change { null } }
        }

        pane.back(context, player)

        return pane.menu(Ui.title("$stateName Look", config.id))
    }

    private fun field(pane: Pane, slot: Int, item: NpcItem, set: Boolean, edit: () -> Unit, clear: () -> Unit) {
        pane.on(slot, item) { click ->
            when (click) {
                MenuClick.LEFT -> edit()
                MenuClick.DROP -> if (set) clear()
                else -> Unit
            }
        }
    }

    private fun <T> shown(value: T?, format: (T) -> String): String? = if (value == null) null else format(value)

    private fun onOff(on: Boolean): String = if (on) "On" else "Off"

    private fun lookField(material: String, label: String, custom: String?, inherited: String, action: String): NpcItem =
        Ui.item(
            material,
            "<ttl>$label",
            listOfNotNull(
                if (custom == null) "<bd>Same as the slot: <val>$inherited" else "<bd>Custom: <val>$custom",
                action,
                if (custom == null) null else "<key>Press Q to use the slot's again",
            ),
        )
}
