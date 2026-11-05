package app.simplecloud.npc.common.editor.npc.confirm

import app.simplecloud.npc.common.editor.core.ConfirmGate
import app.simplecloud.npc.common.editor.core.ConfirmSpec
import app.simplecloud.npc.common.editor.menu.EditorMenu
import app.simplecloud.npc.common.editor.npc.ConfirmPurpose
import app.simplecloud.npc.common.editor.npc.NpcEditorContext
import app.simplecloud.npc.common.editor.npc.NpcEditorScreen
import app.simplecloud.npc.common.editor.npc.NpcFormat
import app.simplecloud.npc.common.editor.npc.Refresh
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.item.NpcItem
import app.simplecloud.npc.core.config.NpcConfig
import app.simplecloud.npc.core.platform.NpcPlayer

object ConfirmGateMenuBuilder {
    private class Wording(val title: String, val verb: String)

    private fun wording(purpose: ConfirmPurpose): Wording = when (purpose) {
        is ConfirmPurpose.RemoveJoinState -> Wording(
            "Remove ${NpcFormat.joinState(purpose.joinState)}?",
            "Remove",
        )

        is ConfirmPurpose.RemoveLayout -> Wording(
            "Remove ${NpcFormat.joinState(purpose.joinState)} layout?",
            "Remove",
        )
    }

    fun build(context: NpcEditorContext, player: NpcPlayer, config: NpcConfig, purpose: ConfirmPurpose): EditorMenu {
        val words = wording(purpose)
        return ConfirmGate.build(
            context,
            player,
            ConfirmSpec<NpcEditorScreen>(
                title = Ui.title(words.title, NpcFormat.displayName(config)),
                screen = NpcEditorScreen.Confirm(config.id, purpose),
                verb = words.verb,
                head = head(config, purpose),
                fire = { fire(context, player, config, purpose) },
            ),
        )
    }

    private fun head(config: NpcConfig, purpose: ConfirmPurpose): NpcItem = when (purpose) {
        is ConfirmPurpose.RemoveJoinState -> {
            val layout = config.hologram.findLayout(purpose.joinState)
            val actionCount = config.actions.count {
                it.joinState.equals(purpose.joinState, true) && it.configuredTypes().isNotEmpty()
            }

            Ui.item(
                "PAPER",
                "<err>Remove ${NpcFormat.joinState(purpose.joinState)}?",
                listOf(
                    "<bd>Hologram: " + if (layout == null) "<off>none" else "<val>${layout.lines.size} lines",
                    "<bd>Actions: <val>$actionCount",
                    "",
                    if (layout == null && actionCount == 0) {
                        "<hnt>Nothing is set up for it yet."
                    } else {
                        "<err>Both are removed. There is no undo."
                    },
                ),
            )
        }

        is ConfirmPurpose.RemoveLayout -> {
            val lines = config.hologram.findLayout(purpose.joinState)?.lines?.size ?: 0
            Ui.item(
                "PAPER",
                "<err>Remove ${NpcFormat.joinState(purpose.joinState)} layout?",
                listOf(
                    "<bd><val>$lines <bd>hologram lines",
                    "<hnt>Actions for ${NpcFormat.joinState(purpose.joinState)} are kept.",
                    "",
                    "<err>The lines are removed. There is no undo.",
                ),
            )
        }
    }

    private fun fire(context: NpcEditorContext, player: NpcPlayer, config: NpcConfig, purpose: ConfirmPurpose) {
        when (purpose) {
            is ConfirmPurpose.RemoveJoinState -> context.commitAndBack(
                player,
                config.id,
                refresh = Refresh.HOLOGRAM,
            ) { fresh ->
                val state = purpose.joinState.lowercase()
                fresh.hologram.layouts.removeIf { it.joinState.equals(state, true) }
                fresh.actions.removeIf { it.joinState.equals(state, true) }
                fresh.joinStates.removeIf { it.equals(state, true) }
                fresh
            }

            is ConfirmPurpose.RemoveLayout -> context.commitAndBack(
                player,
                config.id,
                refresh = Refresh.HOLOGRAM,
            ) { fresh ->
                fresh.hologram.layouts.removeIf { it.joinState.equals(purpose.joinState, true) }
                fresh
            }
        }
    }
}
