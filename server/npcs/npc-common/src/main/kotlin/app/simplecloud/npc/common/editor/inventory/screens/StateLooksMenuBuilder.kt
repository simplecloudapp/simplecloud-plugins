package app.simplecloud.npc.common.editor.inventory.screens

import app.simplecloud.npc.common.editor.core.back
import app.simplecloud.npc.common.editor.inventory.InventoryEditorContext
import app.simplecloud.npc.common.editor.inventory.InventoryEditorScreen
import app.simplecloud.npc.common.editor.inventory.LiveGroups
import app.simplecloud.npc.common.editor.menu.EditorMenu
import app.simplecloud.npc.common.editor.menu.Pane
import app.simplecloud.npc.common.editor.ui.Layout
import app.simplecloud.npc.common.editor.ui.Palette
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.inventory.source.GroupServersSource
import app.simplecloud.npc.common.inventory.view.ItemIconResolver
import app.simplecloud.npc.common.item.NpcItem
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

    fun template(config: InventoryConfiguration, group: String): InventoryItemConfiguration =
        LiveGroups.members(config, group).firstOrNull { it.pageControl == null } ?: InventoryItemConfiguration()

    fun preview(template: InventoryItemConfiguration, state: String, look: StateLook?): NpcItem {
        val styled = ItemIconResolver.icon(if (look == null) template else ItemIconResolver.styled(template, look))
        val summary = if (look == null) {
            listOf("<off>Uses the slot's own look")
        } else {
            val material = look.material
            val name = look.name
            val lore = look.lore
            val glint = look.glowing
            listOfNotNull(
                "<on>Own look set",
                if (material == null) null else "<bd>Material: <val>${Ui.pretty(material)}",
                if (name == null) null else "<bd>Name: <val>${Ui.quote(name)}",
                if (lore == null) null else "<bd>Lore: <val>${lore.size} lines",
                if (glint == null) null else "<bd>Glint: <val>${if (glint) "On" else "Off"}",
            )
        }
        return styled.copy(
            name = Palette.expand("${stateColor(state)}${label(state)}"),
            lore = summary.map(Palette::expand),
            glowing = look?.glowing ?: template.glowing,
            amount = 1,
        )
    }

    fun build(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        group: String,
    ): EditorMenu {
        val source = LiveGroups.source(config, group)
        val template = template(config, group)
        val states = statesFor(source?.type)
        val pane = Pane(SIZE)

        pane[HEADER_SLOT] = Ui.item(
            "PAINTING",
            "<ttl>Looks of $group",
            listOf(
                "<bd>How an entry looks while its server is",
                "<bd>" + states.joinToString(", ") { label(it).lowercase() } + ".",
                "",
                "<hnt>Anything not set keeps the slot's own look.",
            ),
        )

        states.zip(Layout.row(1, states.size)).forEach { (state, slot) ->
            val item = preview(template, state, source?.lookFor(state))
            pane.left(slot, item.copy(lore = item.lore + Palette.expand("") + Palette.expand("<key>Left-click to edit this look"))) {
                context.navigate(player, InventoryEditorScreen.StateLook(config.id, group, state))
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
