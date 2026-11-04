package app.simplecloud.npc.common.editor.inventory.screens

import app.simplecloud.npc.common.editor.core.ActionFieldsPane
import app.simplecloud.npc.common.editor.core.back
import app.simplecloud.npc.common.editor.inventory.ClickSummary
import app.simplecloud.npc.common.editor.inventory.InventoryEditorContext
import app.simplecloud.npc.common.editor.inventory.InventoryEditorScreen
import app.simplecloud.npc.common.editor.inventory.InventoryPickerPurpose
import app.simplecloud.npc.common.editor.menu.EditorMenu
import app.simplecloud.npc.common.editor.menu.Pane
import app.simplecloud.npc.common.editor.ui.Clicks
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.inventory.ItemClickActions
import app.simplecloud.npc.core.config.NpcConfig.ActionConfiguration
import app.simplecloud.npc.core.interaction.PlayerInteraction
import app.simplecloud.npc.core.inventory.InventoryConfiguration
import app.simplecloud.npc.core.platform.NpcPlayer

object ItemActionMenuBuilder {

    fun build(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        screen: InventoryEditorScreen.ItemAction,
    ): EditorMenu {
        val slots = screen.slots
        val pane = Pane(ActionFieldsPane.SIZE)
        val items = config.items.filter { it.slot in slots }

        pane[ActionFieldsPane.HEADER_SLOT] = Ui.item(
            Clicks.material(screen.interaction),
            "<ttl>${Clicks.label(screen.interaction)}",
            listOf("<bd>Editing: <val>" + if (slots.size == 1) "This item" else "${slots.size} items") +
                when {
                    ClickSummary.differs(items, screen.interaction) ->
                        listOf("<warn>The items differ: what you see is", "<warn>the first one, edits apply to all.")

                    ClickSummary.hasOwn(items, screen.interaction) -> emptyList()

                    else -> listOf("") + ClickSummary.lines(config, items, screen.interaction)
                },
        )

        ActionFieldsPane.fill(pane, port(context, player, config, slots, screen.interaction))

        if (!ClickSummary.hasOwn(items, screen.interaction)) {
            pane.back(context, player)
            return pane.menu(Ui.title(Clicks.label(screen.interaction), config.id))
        }

        pane.left(
            ActionFieldsPane.DELETE_SLOT,
            Ui.item(
                "TNT",
                "<err>Clear This Click",
                listOf("<bd>Removes everything it does.", "<hnt>Undo brings it back.", "", "<key>Left-click to clear it"),
            ),
        ) {
            context.change(player, config.id) { fresh ->
                fresh.items
                    .filter { it.slot in slots }
                    .forEach { item -> item.actions.removeIf { it.interactionType == screen.interaction } }
                fresh
            }
            context.back(player)
        }

        pane.back(context, player)

        return pane.menu(Ui.title(Clicks.label(screen.interaction), config.id))
    }

    fun buildBlocks(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        screen: InventoryEditorScreen.ItemActionBlocks,
    ): EditorMenu {
        val pane = Pane(ActionFieldsPane.ADD_SIZE)

        val editing = "<bd>Editing: <val>" + if (screen.slots.size == 1) "This item" else "${screen.slots.size} items"
        ActionFieldsPane.fillAdd(pane, port(context, player, config, screen.slots, screen.interaction), screen.interaction, listOf(editing))
        pane.back(context, player)

        return pane.menu(Ui.title("Add Action", config.id))
    }

    private fun port(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        slots: List<Int>,
        interaction: PlayerInteraction,
    ): ActionFieldsPane.Port {
        val editor = InventoryEditorScreen.ItemAction(config.id, slots, interaction)
        val first = config.items.firstOrNull { it.slot == slots.first() }
        val action = first?.let { ItemClickActions.of(it, interaction) }
            ?: ActionConfiguration(interactionType = interaction)

        fun edit(mutate: (ActionConfiguration) -> Unit) {
            context.change(player, config.id) { fresh ->
                fresh.items.filter { it.slot in slots }.forEach { item ->
                    mutate(ItemClickActions.orCreate(item, interaction))
                    item.actions.removeIf { it.isEmpty() }
                }
                fresh
            }
            context.render(player, editor)
        }

        fun pick(purpose: InventoryPickerPurpose) =
            context.navigate(player, InventoryEditorScreen.Picker(config.id, purpose))

        return ActionFieldsPane.Port(
            scope = ActionFieldsPane.Scope.MENU,
            player = player,
            action = action,
            edit = ::edit,
            toggleJoinTarget = { edit { it.joinTarget = !action.joinTarget } },
            prompt = { context.chatPrompt(player, it) },
            rerender = { context.render(player, editor) },
            pickMenu = { pick(InventoryPickerPurpose.OpenInventory(slots, interaction)) },
            pickServer = { pick(InventoryPickerPurpose.SendToServer(slots, interaction)) },
            pickSound = { pick(InventoryPickerPurpose.Sound(slots, interaction)) },
            openTitle = { context.navigate(player, InventoryEditorScreen.ItemTitle(config.id, slots, interaction)) },
            openAdd = {
                context.navigate(player, InventoryEditorScreen.ItemActionBlocks(config.id, slots, interaction))
            },
            closeAdd = { context.back(player) },
        )
    }
}
