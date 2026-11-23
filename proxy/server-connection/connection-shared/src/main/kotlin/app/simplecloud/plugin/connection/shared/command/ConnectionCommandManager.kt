package app.simplecloud.plugin.connection.shared.command

import app.simplecloud.plugin.connection.shared.config.CommandEntry

interface ConnectionCommandManager {

    fun register(commands: List<CommandEntry>)

    fun unregister()
}
