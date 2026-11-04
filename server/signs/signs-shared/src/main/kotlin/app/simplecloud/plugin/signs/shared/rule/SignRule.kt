package app.simplecloud.plugin.signs.shared.rule

import app.simplecloud.api.server.Server
import app.simplecloud.api.server.ServerState

enum class SignRule {
    STARTING,
    OFFLINE,
    MAINTENANCE,
    FULL,
    EMPTY,
    ONLINE,
    ;

    fun matches(server: Server?): Boolean {
        if (server == null) return this == OFFLINE
        return when (this) {
            STARTING -> server.state == ServerState.STARTING

            OFFLINE -> false

            MAINTENANCE -> server.properties["maintenance"]?.toString().toBoolean() ||
                server.properties["joinstate"]?.toString() == "maintenance"

            FULL -> server.playerCount == server.maxPlayers

            EMPTY -> server.state == ServerState.AVAILABLE && server.playerCount == 0

            ONLINE -> server.state == ServerState.AVAILABLE
        }
    }
}
