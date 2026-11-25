package app.simplecloud.core.shared.controller

import app.simplecloud.api.persistentserver.PersistentServer

fun PersistentServer.getIdentifiers(): Set<String> =
    listOfNotNull(persistentServerId, name)
        .filter { it.isNotBlank() }
        .map { it.lowercase() }
        .toSet()
