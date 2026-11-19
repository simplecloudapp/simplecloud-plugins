package app.simplecloud.plugin.connection.shared.connection

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class KickedServers {

    private val serverNamesByPlayer = ConcurrentHashMap<UUID, Set<String>>()

    fun recordKick(playerId: UUID, serverName: String): Set<String> {
        val serverNames = serverNamesByPlayer.getOrDefault(playerId, emptySet()) + serverName
        serverNamesByPlayer[playerId] = serverNames
        return serverNames
    }

    fun clear(playerId: UUID) {
        serverNamesByPlayer.remove(playerId)
    }
}
