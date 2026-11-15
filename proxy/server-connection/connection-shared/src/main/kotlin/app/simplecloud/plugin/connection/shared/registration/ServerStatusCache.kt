package app.simplecloud.plugin.connection.shared.registration

import java.util.concurrent.ConcurrentHashMap

class ServerStatusCache(
    private val registrations: ServerRegistrations,
) {

    private val statuses = ConcurrentHashMap<String, ServerStatus>()

    fun findAllByProxyName(): Map<String, ServerStatus> = registrations.findAll()
        .filter { server -> registrations.proxyNameOf(server.serverId).isPresent }
        .associate { server -> server.proxyName to (statuses[server.serverId] ?: ServerStatus(false, 0, 0)) }

    fun update(serverId: String, status: ServerStatus) {
        statuses[serverId] = status
    }

    fun markStopping(serverId: String) {
        statuses.computeIfPresent(serverId) { _, status -> status.copy(stopping = true) }
    }

    fun remove(serverId: String) {
        statuses.remove(serverId)
    }
}
