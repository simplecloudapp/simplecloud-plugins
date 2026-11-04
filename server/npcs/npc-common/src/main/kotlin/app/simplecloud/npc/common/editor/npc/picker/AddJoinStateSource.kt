package app.simplecloud.npc.common.editor.npc.picker

import app.simplecloud.npc.common.editor.core.PickerHeader
import app.simplecloud.npc.common.editor.core.PickerOption
import app.simplecloud.npc.common.editor.core.PickerResult
import app.simplecloud.npc.common.editor.npc.NpcEditorContext
import app.simplecloud.npc.common.editor.npc.NpcFormat
import app.simplecloud.npc.common.editor.npc.PickerPurpose
import app.simplecloud.npc.common.editor.npc.Refresh
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.core.cloud.JoinStates
import app.simplecloud.npc.core.config.NpcConfig
import app.simplecloud.npc.core.platform.NpcPlayer

class AddJoinStateSource(override val purpose: PickerPurpose.AddJoinState) : PickerSource {
    private val forHologram = purpose.forHologram

    override val title = "Add Join State"

    override fun header(context: NpcEditorContext, config: NpcConfig) = PickerHeader(
        "PAPER",
        "Add a Join State",
        if (forHologram) {
            listOf("<bd>Pick the join state that gets", "<bd>its own hologram lines.")
        } else {
            listOf("<bd>Pick the join state that gets", "<bd>its own click actions.")
        } + listOf("", "<hnt>A join state is what the target", "<hnt>server is doing, e.g. ingame."),
    )

    private fun present(config: NpcConfig): List<String> =
        if (forHologram) NpcFormat.hologramJoinStates(config) else NpcFormat.actionJoinStates(config)

    override fun options(context: NpcEditorContext, player: NpcPlayer, config: NpcConfig): List<PickerOption> {
        val present = present(config)
        val atCap = !forHologram && present.size >= NpcConfig.MAX_JOIN_STATES
        val liveTargets = config.targetServers
            .mapNotNull { target -> context.liveData.joinState(target)?.let { it.lowercase() to target } }
            .groupBy({ it.first }, { it.second })

        return JoinStates.offered(config, liveTargets.keys).map { state ->
            val label = NpcFormat.joinState(state)
            val count = if (forHologram) {
                config.hologram.findLayout(state)?.lines?.size ?: 0
            } else {
                config.actions.count { it.joinState.equals(state, true) && !it.isEmpty() }
            }
            val usedLine = (if (forHologram) "<bd>Hologram lines: " else "<bd>Clicks set: ") +
                if (count == 0) "<off>None" else "<val>$count"
            val liveLine = liveTargets[state]?.let { "<on>Live now <hnt>on ${it.joinToString(", ")}" }
            val lore = listOfNotNull(usedLine, liveLine)
            when {
                state in present -> PickerOption(
                    label,
                    Ui.unavailable(
                        label,
                        lore,
                        if (NpcFormat.isDefaultJoinState(state)) "Always there: it is the default." else "Already set up here.",
                        "PAPER",
                    ),
                    available = false,
                )

                atCap -> PickerOption(label, Ui.unavailable(label, lore, "All join state slots are used.", "PAPER"), available = false)

                else -> PickerOption(state, Ui.item("PAPER", "<ttl>$label", lore + listOf("", "<key>Left-click to add it")))
            }
        }
    }

    override fun resolveTyped(context: NpcEditorContext, config: NpcConfig, typed: String): PickerResult? {
        val state = typed.removePrefix("@").lowercase()
        val present = present(config)

        return when {
            !JOIN_STATE_PATTERN.matches(state) ->
                PickerResult.Rejected("Invalid", typed, "Letters, digits, _ and - only, up to 24 characters.")

            state in present && forHologram ->
                PickerResult.Rejected("Already present", typed, "That join state is already set up.")

            state in present -> PickerResult.Rejected("Already present", typed, "That join state is already set up.")

            !forHologram && present.size >= NpcConfig.MAX_JOIN_STATES ->
                PickerResult.Rejected(
                    "Cap reached",
                    typed,
                    "All ${NpcConfig.MAX_JOIN_STATES} join state columns are in use.",
                )

            else -> null
        }
    }

    override fun apply(context: NpcEditorContext, player: NpcPlayer, config: NpcConfig, value: String) {
        val state = value.removePrefix("@").lowercase()
        val session = context.session(player)
        if (forHologram) session.hologramState = state else session.actionState = state
        if (forHologram) {
            context.commitAndBack(player, config.id, refresh = Refresh.HOLOGRAM) { fresh ->
                fresh.apply { hologram.layoutOrCreate(state) }
            }
        } else {
            context.commitAndBack(player, config.id) { fresh ->
                if (fresh.joinStates.any { it.equals(state, true) }) return@commitAndBack null
                if (present(fresh).size >= NpcConfig.MAX_JOIN_STATES) return@commitAndBack null

                fresh.joinStates += state
                fresh
            }
        }
    }

    private companion object {
        private val JOIN_STATE_PATTERN = Regex("^[a-z0-9_-]{1,24}$")
    }
}
