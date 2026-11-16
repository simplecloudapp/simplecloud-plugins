package app.simplecloud.plugin.connection.shared.connection

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class KickTracker {

    private val kickedServerNames = ConcurrentHashMap<UUID, Set<String>>()
    private val lastKicks = ConcurrentHashMap<UUID, Long>()

    fun add(playerId: UUID, serverName: String): Set<String> {
        val serverNames = findRecent(playerId) + serverName
        kickedServerNames[playerId] = serverNames
        lastKicks[playerId] = System.currentTimeMillis()
        return serverNames
    }

    fun remove(playerId: UUID) {
        kickedServerNames.remove(playerId)
        lastKicks.remove(playerId)
    }

    private fun findRecent(playerId: UUID): Set<String> {
        val lastKick = lastKicks[playerId] ?: return emptySet()
        if (System.currentTimeMillis() - lastKick > 10_000) return emptySet()
        return kickedServerNames[playerId].orEmpty()
    }
}
