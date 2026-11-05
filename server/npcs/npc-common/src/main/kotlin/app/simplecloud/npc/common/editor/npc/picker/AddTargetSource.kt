package app.simplecloud.npc.common.editor.npc.picker

import app.simplecloud.npc.common.editor.core.EmptyState
import app.simplecloud.npc.common.editor.core.PickerHeader
import app.simplecloud.npc.common.editor.core.PickerOption
import app.simplecloud.npc.common.editor.core.PickerOptions
import app.simplecloud.npc.common.editor.core.PickerResult
import app.simplecloud.npc.common.editor.core.ServerStatus
import app.simplecloud.npc.common.editor.npc.NpcEditorContext
import app.simplecloud.npc.common.editor.npc.NpcFormat
import app.simplecloud.npc.common.editor.npc.PickerPurpose
import app.simplecloud.npc.common.editor.npc.Refresh
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.core.config.NpcConfig
import app.simplecloud.npc.core.platform.NpcPlayer

object AddTargetSource : PickerSource {
    override val purpose = PickerPurpose.AddTarget
    override val title = "Add Target"

    override fun header(context: NpcEditorContext, config: NpcConfig) = PickerHeader(
        "COMPASS",
        "Add a Target",
        listOf(
            "<bd>Pick a group or server that",
            "<val>${NpcFormat.displayName(config)} <bd>sends players to.",
            "",
            "<bd>Targets: <val>${config.targetServers.size} <bd>of <val>${NpcConfig.MAX_TARGETS}",
        ),
    )

    override val empty = EmptyState(
        "No Groups Or Servers",
        listOf("<bd>The cloud has none yet, or type", "<bd>the name of one below."),
    )

    override fun options(context: NpcEditorContext, player: NpcPlayer, config: NpcConfig): List<PickerOption> {
        val live = context.liveData
        val servers = live.servers()
        val atCap = config.targetServers.size >= NpcConfig.MAX_TARGETS

        return live.knownTargets().map { target ->
            val typeLine = "<bd>Type: <val>${if (target.group) "Group" else "Server"}"
            val used = config.targetServers.any { it.equals(target.name, true) }
            when {
                used -> PickerOption(
                    target.name,
                    Ui.unavailable(target.name, listOf(typeLine), "Already a target.", if (target.group) "BOOKSHELF" else "MAP"),
                    available = false,
                )

                atCap -> PickerOption(
                    target.name,
                    Ui.unavailable(target.name, listOf(typeLine), "All ${NpcConfig.MAX_TARGETS} target slots are used.", if (target.group) "BOOKSHELF" else "MAP"),
                    available = false,
                )

                target.group -> PickerOption(
                    target.name,
                    Ui.item(
                        "BOOKSHELF",
                        "<ttl>${target.name}",
                        listOf(
                            typeLine,
                            "<bd>Servers: <val>${live.summaryOf(target.name).liveServers}",
                            "",
                            "<key>Left-click to add this group",
                        ),
                    ),
                )

                else -> {
                    val status = servers.firstOrNull { it.name.equals(target.name, true) }?.status
                        ?: ServerStatus.OFFLINE
                    PickerOption(
                        target.name,
                        Ui.item(
                            "MAP",
                            "<ttl>${target.name}",
                            listOf(
                                typeLine,
                                "<bd>Status: ${PickerOptions.statusText(status)}",
                                "",
                                "<key>Left-click to add this server",
                            ),
                        ),
                    )
                }
            }
        }
    }

    override fun resolveTyped(context: NpcEditorContext, config: NpcConfig, typed: String): PickerResult? = when {
        config.targetServers.any { it.equals(typed, true) } ->
            PickerResult.Rejected("Already targeted", typed, "This NPC already has that target.")

        config.targetServers.size >= NpcConfig.MAX_TARGETS ->
            PickerResult.Rejected("Cap reached", typed, "All ${NpcConfig.MAX_TARGETS} target slots are in use.")

        else -> PickerOptions.match(
            context.liveData.knownTargets().map { it.name },
            typed,
            "No group or server by that name.",
        )
    }

    override fun apply(context: NpcEditorContext, player: NpcPlayer, config: NpcConfig, value: String) {
        val canonical = context.liveData.knownTargets().firstOrNull { it.name.equals(value, true) }?.name ?: value
        var count = config.targetServers.size
        val added = context.change(player, config.id, Refresh.HOLOGRAM) { fresh ->
            if (fresh.targetServers.size >= NpcConfig.MAX_TARGETS) return@change null
            if (fresh.targetServers.any { it.equals(canonical, true) }) return@change null

            fresh.targetServers += canonical
            count = fresh.targetServers.size
            fresh
        }
        val result = if (added) {
            PickerResult.Added(canonical, "Now targeting $count of ${NpcConfig.MAX_TARGETS}.")
        } else {
            PickerResult.Rejected("Not added", canonical, "Already targeted, or all target slots are in use.")
        }
        PickerMenuBuilder.showResult(context, player, config, purpose, result)
    }
}
