package app.simplecloud.plugin.connection.shared.platform

import app.simplecloud.plugin.connection.shared.registration.ServerRegistry
import java.io.File

interface ConnectionPlatform<S : ConnectionServer> {

    fun getDataDirectory(): File

    fun getRegistry(): ServerRegistry

    fun getServers(): List<S>
}
