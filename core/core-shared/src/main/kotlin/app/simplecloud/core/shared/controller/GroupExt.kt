package app.simplecloud.core.shared.controller

import app.simplecloud.api.group.Group

fun Group.getMinServers(): Int = scaling?.minServers() ?: 0

fun Group.getMaxServers(): Int? = scaling?.maxServers()
