package app.simplecloud.plugin.connection.shared.platform

import app.simplecloud.plugin.connection.shared.command.ConnectionCommandManager
import app.simplecloud.plugin.connection.shared.registration.ServerRegistry
import java.io.File

interface ConnectionPlatform {

    fun getDataDirectory(): File

    fun getRegistry(): ServerRegistry

    fun getCommandManager(): ConnectionCommandManager

    fun getServers(): List<ConnectionServer>
}
