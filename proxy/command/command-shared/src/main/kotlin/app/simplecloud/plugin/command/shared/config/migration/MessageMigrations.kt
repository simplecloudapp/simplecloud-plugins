package app.simplecloud.plugin.command.shared.config.migration

import app.simplecloud.plugin.command.shared.config.HelpSection
import app.simplecloud.plugin.command.shared.utilities.ConfigVersion
import app.simplecloud.core.shared.config.migration.ConfigMigration
import app.simplecloud.core.shared.config.migration.ConfigMigrations

object MessageMigrations : ConfigMigrations(ConfigVersion.VERSION) {

    private const val DESCRIPTIONS = "command.help.descriptions"

    override val migrations: List<ConfigMigration> = listOf(
        rename(2, "$DESCRIPTIONS.group stop <group> [id]", "group stop <group> [ids]"),
        rename(2, "$DESCRIPTIONS.server info <group> <id>", "server info <server>"),
        rename(2, "$DESCRIPTIONS.server stop <group> <id>", "server stop <server>"),
        rename(2, "$DESCRIPTIONS.player list [<group|ps> <target>]", "player list [<group|ps|server> <target>] [page]"),
        rename(2, "$DESCRIPTIONS.server list [group]", "server list [group] [page]"),
        rename(2, "$DESCRIPTIONS.player send <player> <group|ps> <target>", "player send <player> <group|ps|server> <target> [mode]"),
        rename(2, "$DESCRIPTIONS.player sendall <group|ps> <target>", "player sendall <group|ps|server> <target> [mode]"),
        rename(2, "$DESCRIPTIONS.player sendfrom <source> <group|ps> <target>", "player sendfrom <source> <group|ps|server> <target> [mode]"),
        action(2, DESCRIPTIONS) { _, node ->
            HelpSection().descriptions.forEach { (usage, description) ->
                if (node.node(usage).virtual()) node.node(usage).set(description)
            }
            null
        },
        replaceDefault(
            2,
            "command.usage.invalid-target-type",
            "<prefix> <error>Use group or ps as the target type.",
            "<prefix> <error>Use group, ps or server as the target type.",
        ),
        replaceDefault(
            2,
            "command.group.stop.success",
            "<prefix> <info>Sent the stop request for the servers of group <group>.",
            "<prefix> <info>Sent the stop request for <count> servers of group <group>.",
        ),
        replaceDefault(
            2,
            "command.server.stop.success",
            "<prefix> <info>Sent the stop request for server <group>-<id>.",
            "<prefix> <info>Sent the stop request for server <server>.",
        ),
        replaceDefault(
            2,
            "command.server.error.not-found",
            "<prefix> <error>Server <group>-<id> was not found.",
            "<prefix> <error>Server <server> was not found.",
        ),
        replaceDefault(
            2,
            "command.server.error.already-stopping",
            "<prefix> <warning>Server <group>-<id> is already stopping.",
            "<prefix> <warning>Server <server> is already stopping.",
        ),
        remove(2, "command.group.rollout.error.start-failed"),
        remove(2, "command.group.rollout.error.cancel-failed"),
        remove(2, "command.group.rollout.error.status-failed"),
        remove(2, "command.server.error.start-failed"),
        remove(2, "command.server.error.stop-failed"),
        remove(2, "command.player.error.message-failed"),
    )
}
