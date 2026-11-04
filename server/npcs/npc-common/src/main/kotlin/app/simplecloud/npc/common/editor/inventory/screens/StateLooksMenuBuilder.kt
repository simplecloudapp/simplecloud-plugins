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
import app.simplecloud.npc.common.editor.ui.Palette
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.inventory.source.GroupServersSource
import app.simplecloud.npc.common.inventory.view.ItemIconResolver
import app.simplecloud.npc.core.inventory.InventoryConfiguration
import app.simplecloud.npc.core.inventory.InventoryConfiguration.EntryStates
import app.simplecloud.npc.core.inventory.InventoryConfiguration.InventoryItemConfiguration
import app.simplecloud.npc.core.inventory.InventoryConfiguration.StateLook
import app.simplecloud.npc.core.platform.NpcPlayer

object StateLooksMenuBuilder {
    private const val SIZE = Layout.SIZE

    private const val HEADER_SLOT = 4
    private const val NO_STATES_SLOT = 22

    fun statesFor(sourceType: String?): List<String> = when (sourceType) {
        null, "static" -> emptyList()
        GroupServersSource.TYPE -> EntryStates.SERVER_STATES
        else -> EntryStates.TARGET_STATES
    }

    fun build(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        group: String,
    ): EditorMenu {
        val screen = InventoryEditorScreen.StateLooks(config.id, group)
        val source = LiveGroups.source(config, group)
        val template = LiveGroups.members(config, group).firstOrNull { it.pageControl == null }
            ?: InventoryItemConfiguration()
        val states = statesFor(source?.type)
        val pane = Pane(SIZE)

        fun change(state: String, transform: (StateLook) -> StateLook?) {
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
                        PromptResult.rejectInvalidMiniMessage(text)?.let { return@Prompt it }
                        apply(text)
                        PromptResult.Accepted
                    },
                ),
            )
        }

        pane[HEADER_SLOT] = Ui.item(
            "PAINTING",
            "<ttl>Looks Of $group",
            listOf(
                "<bd>How an entry looks while its server is",
                "<bd>" + states.joinToString(", ") { label(it).lowercase() } + ".",
                "",
                "<hnt>Anything not set keeps the slot's own look.",
            ),
        )

        states.zip(Layout.row(1, states.size)).forEach { (state, slot) ->
            val look = source?.lookFor(state)
            val preview = ItemIconResolver.icon(look?.let { ItemIconResolver.styled(template, it) } ?: template)
            val glowing = look?.glowing ?: template.glowing
            val lookMaterial = look?.material
            val lookName = look?.name
            val lookLore = look?.lore
            val lookGlint = look?.glowing
            pane.on(
                slot,
                preview.copy(
                    name = Palette.expand("${stateColor(state)}${label(state)}"),
                    lore = listOfNotNull(
                        if (look == null) "<off>Uses the slot's own look" else "<on>Own look set",
                        if (lookMaterial == null) null else "<bd>Material: <val>${Ui.pretty(lookMaterial)}",
                        if (lookName == null) null else "<bd>Name: <val>${Ui.quote(lookName)}",
                        if (lookLore == null) null else "<bd>Lore: <val>${lookLore.size} lines",
                        if (lookGlint == null) null else "<bd>Glint: <val>${if (lookGlint) "On" else "Off"}",
                        "",
                        "<key>Left-click to change the material",
                        "<key>Right-click to change the name",
                        "<key>Shift-click to turn the glint ${if (glowing) "off" else "on"}",
                        "<key>Shift-right-click to change the lore",
                        "<key>Press Q to go back to the slot's look".takeIf { look != null },
                    ).map(Palette::expand),
                    glowing = glowing,
                    amount = 1,
                ),
            ) { click ->
                when (click) {
                    MenuClick.LEFT -> context.navigate(
                        player,
                        InventoryEditorScreen.Picker(config.id, InventoryPickerPurpose.LookMaterial(group, state)),
                    )

                    MenuClick.RIGHT -> prompt(
                        "Look Name",
                        "Type the name entries show while ${label(state).lowercase()}.",
                        look?.name ?: template.name,
                    ) { text -> change(state) { it.copy(name = text) } }

                    MenuClick.SHIFT_LEFT -> change(state) { it.copy(glowing = !glowing) }

                    MenuClick.SHIFT_RIGHT -> prompt(
                        "Look Lore",
                        "Type the lore in chat, split lines with |.",
                        (look?.lore ?: template.lore).joinToString(" | "),
                    ) { text ->
                        change(state) { it.copy(lore = text.split('|').map(String::trim).toMutableList()) }
                    }

                    MenuClick.DROP -> if (look != null) change(state) { null }

                    else -> Unit
                }
            }
        }

        if (states.isEmpty()) {
            pane[NO_STATES_SLOT] = Ui.disabled("No States", listOf("<hnt>Hand-written entries have no server state."))
        }

        pane.back(context, player)

        return pane.menu(Ui.title("Looks of $group", config.id))
    }

    private fun stateColor(state: String): String = when (state) {
        "AVAILABLE" -> "<success>"
        EntryStates.FULL -> "<error>"
        "INGAME" -> "<warning>"
        EntryStates.OFFLINE -> "<muted>"
        else -> "<info>"
    }

    fun label(state: String): String = when (state) {
        EntryStates.FULL -> "Full"
        EntryStates.OFFLINE -> "Offline"
        else -> state.lowercase().replaceFirstChar(Char::uppercase)
    }
}
