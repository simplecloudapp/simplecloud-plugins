package app.simplecloud.npc.common.editor.inventory.screens

import app.simplecloud.npc.common.editor.Prompt
import app.simplecloud.npc.common.editor.PromptFormat
import app.simplecloud.npc.common.editor.PromptPlaceholder
import app.simplecloud.npc.common.editor.PromptResult
import app.simplecloud.npc.common.editor.PromptTokens
import app.simplecloud.npc.common.editor.core.LineList
import app.simplecloud.npc.common.editor.core.LinesPane
import app.simplecloud.npc.common.editor.core.back
import app.simplecloud.npc.common.editor.inventory.InventoryEditorContext
import app.simplecloud.npc.common.editor.inventory.InventoryEditorScreen
import app.simplecloud.npc.common.editor.inventory.StudioTargets
import app.simplecloud.npc.common.editor.menu.EditorMenu
import app.simplecloud.npc.common.editor.menu.Pane
import app.simplecloud.npc.common.editor.ui.Layout
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.text.Msg
import app.simplecloud.npc.core.inventory.InventoryConfiguration
import app.simplecloud.npc.core.platform.NpcPlayer

object LoreMenuBuilder {
    private const val NOTE_SLOT = Layout.HEADER

    fun build(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        screen: InventoryEditorScreen.Lore,
    ): EditorMenu {
        val targets = StudioTargets.of(config, screen.slots, context.session(player).thisSlotOnly)
        val first = config.items.firstOrNull { it.slot == screen.slots.first() }
        val pane = Pane(LinesPane.SIZE)

        val lines = LineList(
            lines = first?.lore.orEmpty(),
            textOf = { it },
            withText = { _, text -> text },
            create = { it },
            edit = { mutate ->
                context.change(player, config.id) { cfg ->
                    val updated = cfg.items.firstOrNull { it.slot == screen.slots.first() }?.lore.orEmpty()
                        .toMutableList()
                    if (!mutate(updated)) return@change null
                    StudioTargets.edit(cfg, targets) { it.copy(lore = updated.toMutableList()) }
                }
                context.render(player, screen)
            },
            prompt = { _, current, onText, onDelete ->
                val tokens = PromptTokens.MENU + PromptTokens.ENTRY.takeIf { first?.liveGroup != null }.orEmpty()
                promptLine(context, player, screen, current, tokens, onText, onDelete)
            },
        )
        LinesPane.place(pane, lines)
        val differ = StudioTargets.shared(StudioTargets.items(config, targets)) { it.lore } is StudioTargets.Shared.Mixed
        pane[NOTE_SLOT] = Ui.item(
            "WRITABLE_BOOK",
            "<ttl>Lore",
            listOfNotNull(
                "<bd>The lines under the item name.",
                "",
                "<bd>Editing: <val>" + if (targets.size == 1) "This slot" else "${targets.size} slots",
                if (differ) "<warn>Their lore differs: the first item's" else null,
                if (differ) "<warn>lines replace it on all of them." else null,
            ),
        )
        pane.back(context, player)

        return pane.menu(Ui.title("Lore", config.id))
    }

    private fun promptLine(
        context: InventoryEditorContext,
        player: NpcPlayer,
        screen: InventoryEditorScreen.Lore,
        current: String?,
        placeholders: List<PromptPlaceholder>,
        apply: (String) -> Unit,
        onDelete: (() -> Unit)?,
    ) {
        context.chatPrompt(
            player,
            Prompt(
                title = "Lore Line",
                instruction = "Type the lore line in chat.",
                format = PromptFormat.MINI_MESSAGE,
                placeholders = placeholders,
                current = current,
                onClear = onDelete,
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
}
