package app.simplecloud.plugin.signs.shared

import app.simplecloud.api.proxy.ProxyServerNames
import app.simplecloud.api.server.Server

object ServerNames {

    fun displayName(server: Server?): String = when {
        server == null -> ""
        server.isFromGroup -> "${groupName(server)}-${server.numericalId}"
        server.isFromPersistentServer -> persistentServerName(server).ifEmpty { server.serverId }
        else -> server.serverBase?.name ?: server.serverId
    }

    fun proxyName(server: Server, pattern: String): String = ProxyServerNames.proxyNameOf(server.serverId).orElseGet {
        pattern
            .replace("%server-name%", displayName(server))
            .replace("%group%", groupName(server))
            .replace("%numerical-id%", server.numericalId.toString())
            .replace("%persistent-server%", persistentServerName(server))
    }

    fun groupName(server: Server): String = server.group?.name ?: server.serverGroupId.orEmpty()

    fun persistentServerName(server: Server): String =
        server.persistentServer?.name ?: server.persistentServerId.orEmpty()
}
