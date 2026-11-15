package app.simplecloud.plugin.connection.shared.connection

import app.simplecloud.plugin.connection.shared.command.ConnectionCommandManager
import app.simplecloud.plugin.connection.shared.platform.ConnectionPlatform
import app.simplecloud.plugin.connection.shared.platform.ConnectionServer
import app.simplecloud.plugin.connection.shared.registration.ServerRegistry
import app.simplecloud.plugin.connection.shared.registration.TestServerRegistry
import java.io.File

class TestConnectionPlatform(
    private val servers: List<ConnectionServer>,
) : ConnectionPlatform {

    private val registry = TestServerRegistry()

    override fun getDataDirectory(): File = error("The data directory is not used in tests")

    override fun getRegistry(): ServerRegistry = registry

    override fun getCommandManager(): ConnectionCommandManager = error("The command manager is not used in tests")

    override fun getServers(): List<ConnectionServer> = servers
}
