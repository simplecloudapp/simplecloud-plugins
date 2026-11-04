package app.simplecloud.npc.common.editor.npc.tabs

import app.simplecloud.npc.common.editor.Prompt
import app.simplecloud.npc.common.editor.PromptResult
import app.simplecloud.npc.common.editor.core.armed
import app.simplecloud.npc.common.editor.menu.EditorMenu
import app.simplecloud.npc.common.editor.npc.NpcEditorContext
import app.simplecloud.npc.common.editor.npc.NpcEditorScreen
import app.simplecloud.npc.common.editor.npc.NpcFormat
import app.simplecloud.npc.common.editor.npc.NpcTab
import app.simplecloud.npc.common.editor.ui.Layout
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.manager.NpcFailure
import app.simplecloud.npc.common.manager.NpcOperationResult
import app.simplecloud.npc.core.config.ConfigIds
import app.simplecloud.npc.core.config.NpcConfig
import app.simplecloud.npc.core.platform.NpcPlayer
import net.kyori.adventure.text.Component

object NpcTabBuilder {
    private const val SUMMON_SLOT = 21
    private const val DUPLICATE_SLOT = 23
    private const val STATUS_SLOT = 31
    private const val DELETE_SLOT = Layout.TOOL_RIGHT

    fun build(context: NpcEditorContext, player: NpcPlayer, config: NpcConfig): EditorMenu {
        val screen = NpcEditorScreen.Tab(config.id, NpcTab.NPC)
        val live = context.liveData
        val pane = TabFrame.pane(context, player, config, NpcTab.NPC)

        pane.left(
            SUMMON_SLOT,
            Ui.item(
                "ENDER_PEARL",
                "<ttl>Summon to Me",
                listOf("<bd>Now at <val>${NpcFormat.position(config.entity.location)}", "<key>Left-click to move it here"),
            ),
        ) {
            context.npcManager.teleport(config.id, player.location())
            context.render(player, screen)
        }

        pane.left(
            DUPLICATE_SLOT,
            Ui.item(
                "SPAWNER",
                "<ttl>Duplicate",
                listOf("<bd>A copy appears where you stand.", "<key>Left-click to name the copy"),
            ),
        ) { promptDuplicate(context, player, config, screen) }

        pane.armed(
            DELETE_SLOT,
            context,
            player,
            screen,
            "delete-npc",
            idle = Ui.item("BARRIER", "<err>Delete NPC", listOf("<err>There is no undo.", "", "<key>Left-click to delete it")),
            verb = "Delete ${NpcFormat.displayName(config)}",
        ) {
            context.npcManager.delete(config.id)
            context.close(player)
        }

        val targets = config.targetServers
        val joinState = live.hologramState(config).joinState
        val nearby = live.playersInRange(config)
        pane[STATUS_SLOT] = Ui.item(
            "BOOK",
            "<ttl>Status",
            listOf(
                "<bd>Targets: " + if (targets.isEmpty()) "<off>None" else "<val>" + targets.joinToString("<bd>, <val>"),
                "<bd>Join state: " + if (joinState == null) "<off>Unknown" else "<val>${NpcFormat.joinState(joinState)}",
                "<bd>Players nearby: <val>$nearby",
            ),
        )

        return TabFrame.menu(pane, config, NpcTab.NPC)
    }

    private fun promptDuplicate(
        context: NpcEditorContext,
        player: NpcPlayer,
        config: NpcConfig,
        screen: NpcEditorScreen,
    ) {
        context.chatPrompt(
            player,
            Prompt(
                title = "Duplicate NPC",
                instruction = "Type the id of the copy in chat.",
                onCancel = { context.render(player, screen) },
                onSubmit = { input ->
                    when (val result = context.npcManager.duplicate(config.id, input.trim(), player.location())) {
                        is NpcOperationResult.Success -> {
                            context.replace(player, NpcEditorScreen.Tab(result.config.id, NpcTab.NPC))
                            PromptResult.Accepted
                        }

                        is NpcOperationResult.Failure -> PromptResult.Rejected(
                            Component.text(
                                when (result.failure) {
                                    NpcFailure.ALREADY_EXISTS -> "That id is taken."
                                    NpcFailure.INVALID_ID -> ConfigIds.DESCRIPTION
                                    else -> "The copy could not be created."
                                },
                            ),
                        )
                    }
                },
            ),
        )
    }
}
