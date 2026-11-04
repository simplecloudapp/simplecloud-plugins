package app.simplecloud.npc.common.editor.inventory.screens

import app.simplecloud.npc.common.editor.Prompt
import app.simplecloud.npc.common.editor.PromptResult
import app.simplecloud.npc.common.editor.inventory.InventoryConfirmPurpose
import app.simplecloud.npc.common.editor.inventory.InventoryEditorContext
import app.simplecloud.npc.common.editor.inventory.InventoryEditorScreen
import app.simplecloud.npc.common.editor.inventory.InventoryPickerPurpose
import app.simplecloud.npc.common.editor.menu.EditorMenu
import app.simplecloud.npc.common.editor.menu.MenuClick
import app.simplecloud.npc.common.editor.menu.Pane
import app.simplecloud.npc.common.editor.ui.Layout
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.text.sendSuccess
import app.simplecloud.npc.core.config.ConfigIds
import app.simplecloud.npc.core.config.NpcConfig
import app.simplecloud.npc.core.inventory.InventoryConfiguration
import app.simplecloud.npc.core.platform.NpcPlayer
import net.kyori.adventure.text.Component

object InventoryHubMenuBuilder {
    private const val SIZE = Layout.SIZE

    private const val SNAPSHOT_SLOT = Layout.HEADER
    private const val CANVAS_SLOT = 20
    private const val TITLE_SLOT = 22
    private const val RENAME_SLOT = 24
    private const val USED_BY_SLOT = 30
    private const val DUPLICATE_SLOT = 32
    private const val UNDO_SLOT = Layout.TOOL_LEFT
    private const val DELETE_SLOT = Layout.TOOL_RIGHT

    private const val DEFAULT_TITLE = "Menu"
    private const val USED_BY_LISTED = 6

    fun usedBy(context: InventoryEditorContext, inventoryId: String): List<NpcConfig> =
        context.npcRepository.findAll()
            .filter { npc -> npc.actions.any { it.openInventory.equals(inventoryId, true) } }
            .sortedBy { it.id.lowercase() }

    fun menusOpening(context: InventoryEditorContext, inventoryId: String): List<InventoryConfiguration> =
        context.inventoryRepository.findAll()
            .filter { menu -> !menu.id.equals(inventoryId, true) && opensMenu(menu, inventoryId) }

    private fun opensMenu(menu: InventoryConfiguration, inventoryId: String): Boolean =
        actionsOf(menu).any { it.openInventory.equals(inventoryId, true) }

    private fun actionsOf(menu: InventoryConfiguration): List<NpcConfig.ActionConfiguration> =
        menu.items.flatMap { item -> item.actions + item.live?.entries.orEmpty().flatMap { it.actions } }

    fun build(context: InventoryEditorContext, player: NpcPlayer, config: InventoryConfiguration): EditorMenu {
        val screen = InventoryEditorScreen.Hub(config.id)
        val users = usedBy(context, config.id)
        val menus = menusOpening(context, config.id)
        val history = context.session(player).history(config.id)
        val pane = Pane(SIZE)

        pane[SNAPSHOT_SLOT] = Ui.item(
            "CHEST",
            "<ttl>${config.id}",
            listOf(
                "<bd>Title: <val>" + config.title,
                "<bd>Rows: <val>${config.rows}",
                "<bd>Fixed items: <val>${config.items.count { it.liveGroup == null }}",
                "<bd>Live groups: <val>${config.liveGroups().size}",
                "<bd>Opened by: <val>${users.size} <bd>NPC${if (users.size == 1) "" else "s"}, " +
                    "<val>${menus.size} <bd>menu${if (menus.size == 1) "" else "s"}",
            ),
        )

        pane.left(
            CANVAS_SLOT,
            Ui.item(
                "CRAFTING_TABLE",
                "<ttl>Edit Slots",
                listOf(
                    "<bd>Place, move and style the items",
                    "<bd>of the menu in a live preview.",
                    "",
                    "<key>Left-click to open it",
                ),
            ),
        ) { context.navigate(player, InventoryEditorScreen.Canvas(config.id)) }

        pane.on(
            TITLE_SLOT,
            Ui.item(
                "NAME_TAG",
                "<ttl>Inventory Title",
                listOf(
                    "<bd>Now: <val>" + config.title,
                    "<bd>The text at the top of the menu.",
                    "<hnt>Plain text keeps the vanilla look.",
                    "",
                    "<key>Left-click to type a new one in chat",
                    "<key>Press Q to reset to the default",
                ),
            ),
        ) { click ->
            when (click) {
                MenuClick.LEFT -> context.promptTitle(player, config.id, InventoryEditorScreen.Hub(config.id))

                MenuClick.DROP -> if (context.change(player, config.id) { it.copy(title = DEFAULT_TITLE) }) {
                    context.render(player, screen)
                }

                else -> Unit
            }
        }

        pane.left(
            RENAME_SLOT,
            Ui.item(
                "ANVIL",
                "<ttl>Menu Id",
                listOf(
                    "<bd>Id: <val>${config.id}",
                    "<bd>The name commands and NPCs use.",
                    "<hnt>The title above stays the same.",
                    "",
                    "<key>Left-click to type a new id in chat",
                ),
            ),
        ) { promptRename(context, player, config) }

        if (users.isEmpty()) {
            pane[USED_BY_SLOT] = Ui.item("ARMOR_STAND", "<ttl>Used By", listOf("<off>No NPC opens this menu yet."))
        }
        if (users.isNotEmpty()) {
            pane.left(
                USED_BY_SLOT,
                Ui.item(
                    "ARMOR_STAND",
                    "<ttl>Used By",
                    users.take(USED_BY_LISTED).map { "<bd>· <val>${it.id}" } +
                        listOfNotNull(
                            (users.size - USED_BY_LISTED).takeIf { it > 0 }?.let { "<hnt>and $it more" },
                            "",
                            "<key>Left-click to open one of them",
                        ),
                ),
            ) { context.navigate(player, InventoryEditorScreen.Picker(config.id, InventoryPickerPurpose.UsedBy)) }
        }

        pane.left(
            DUPLICATE_SLOT,
            Ui.item(
                "MAP",
                "<ttl>Duplicate",
                listOf("<bd>Saves a copy as <val>${config.id}-copy", "<bd>and opens it here.", "", "<key>Left-click to copy it"),
            ),
        ) { duplicate(context, player, config) }

        pane.left(
            DELETE_SLOT,
            Ui.item(
                "RED_CONCRETE",
                "<err>Delete This Menu",
                listOf(
                    if (users.isEmpty() && menus.isEmpty()) {
                        "<hnt>No NPC or menu opens it."
                    } else {
                        "<warn>${users.size + menus.size} NPCs or menus still open it."
                    },
                    "",
                    "<key>Left-click to delete it",
                ),
            ),
        ) {
            val confirm = InventoryEditorScreen.Confirm(config.id, InventoryConfirmPurpose.DeleteInventory)
            context.navigate(player, confirm)
        }

        if (!history.canUndo && !history.canRedo) {
            pane[UNDO_SLOT] = Ui.disabled("Undo", listOf("<hnt>Nothing to undo yet."), "RECOVERY_COMPASS")
        } else {
            pane.on(UNDO_SLOT, undoItem(history.undoCount, history.redoCount)) { click ->
                val moved = when (click) {
                    MenuClick.LEFT -> context.undo(player, config.id)
                    MenuClick.RIGHT -> context.redo(player, config.id)
                    else -> false
                }
                if (moved) context.render(player, screen)
            }
        }

        return pane.menu(Ui.root(config.id))
    }

    private fun undoItem(back: Int, forward: Int) = Ui.item(
        "RECOVERY_COMPASS",
        "<ttl>Undo",
        listOfNotNull(
            "<bd>Steps back: <val>$back",
            "<bd>Steps forward: <val>$forward",
            "",
            if (back > 0) "<key>Left-click to undo" else null,
            if (forward > 0) "<key>Right-click to redo" else null,
        ),
    )

    private fun promptRename(context: InventoryEditorContext, player: NpcPlayer, config: InventoryConfiguration) {
        context.chatPrompt(
            player,
            Prompt(
                title = "Rename Menu",
                instruction = "Type the new id in chat.",
                current = config.id,
                onCancel = { context.render(player, InventoryEditorScreen.Hub(config.id)) },
                onSubmit = { input ->
                    val newId = input.trim()
                    when {
                        !ConfigIds.isValid(newId) -> PromptResult.Rejected(Component.text(ConfigIds.DESCRIPTION))

                        newId.equals(config.id, true) ->
                            PromptResult.Rejected(Component.text("That is the current id."))

                        context.inventoryRepository.find(newId) != null ->
                            PromptResult.Rejected(Component.text("A menu called $newId already exists."))

                        else -> {
                            rename(context, config.id, newId)
                            player.sendSuccess("Renamed menu {} to {}.", config.id, newId)
                            context.reset(player, listOf(InventoryEditorScreen.Hub(newId)))
                            PromptResult.Accepted
                        }
                    }
                },
            ),
        )
    }

    private fun rename(context: InventoryEditorContext, oldId: String, newId: String) {
        val current = context.inventoryRepository.find(oldId) ?: return
        val others = menusOpening(context, oldId)
        context.inventoryRepository.save(relinked(current.copy(id = newId), oldId, newId))
        context.inventoryRepository.delete(current.id)
        others.forEach { context.inventoryRepository.save(relinked(it, oldId, newId)) }

        usedBy(context, oldId).forEach { npc ->
            npc.actions.filter { it.openInventory.equals(oldId, true) }.forEach { it.openInventory = newId }
            context.npcRepository.save(npc)
        }
    }

    private fun relinked(menu: InventoryConfiguration, oldId: String, newId: String): InventoryConfiguration =
        menu.apply {
            actionsOf(this).filter { it.openInventory.equals(oldId, true) }.forEach { it.openInventory = newId }
        }

    private fun duplicate(context: InventoryEditorContext, player: NpcPlayer, config: InventoryConfiguration) {
        val id = generateSequence(1) { it + 1 }
            .map { if (it == 1) "${config.id}-copy" else "${config.id}-copy-$it" }
            .first { context.inventoryRepository.find(it) == null }
        context.inventoryRepository.save(config.deepCopy().copy(id = id))
        player.sendSuccess("Duplicated menu {} as {}.", config.id, id)
        context.reset(player, listOf(InventoryEditorScreen.Hub(id)))
    }
}
