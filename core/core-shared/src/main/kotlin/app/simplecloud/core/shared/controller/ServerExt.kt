package app.simplecloud.core.shared.controller

import app.simplecloud.api.server.Server
import app.simplecloud.api.server.ServerState

fun Server.isStopped(): Boolean = state == ServerState.STOPPED

fun Server.isRunning(): Boolean = state !in setOf(ServerState.STOPPING, ServerState.CLEANUP, ServerState.STOPPED)

fun Server.isJoinable(): Boolean = state == ServerState.AVAILABLE || state == ServerState.INGAME

fun Server.getDisplayName(): String {
    val groupName = group?.name
    val persistentServerName = persistentServer?.name
    return when {
        !groupName.isNullOrBlank() -> "$groupName-$numericalId"
        !serverGroupId.isNullOrBlank() -> "$serverGroupId-$numericalId"
        !persistentServerName.isNullOrBlank() -> persistentServerName
        !persistentServerId.isNullOrBlank() -> persistentServerId.orEmpty()
        else -> serverId
    }
}

fun Server.getIdentifiers(): Set<String> {
    val groupName = group?.name ?: serverGroupId
    val numberedName = if (groupName.isNullOrBlank()) null else "$groupName-$numericalId"
    return listOfNotNull(serverId, persistentServerId, persistentServer?.name, numberedName)
        .filter { it.isNotBlank() }
        .map { it.lowercase() }
        .toSet()
}
