package app.simplecloud.plugin.connection.shared.connection

import app.simplecloud.plugin.connection.shared.platform.ConnectionPlatform
import app.simplecloud.plugin.connection.shared.registration.ServerRegistry
import java.io.File

class FakePlatform(
    private val servers: List<FakeServer>,
) : ConnectionPlatform<FakeServer> {

    override fun getDataDirectory(): File = error("Not used by the resolver")

    override fun getRegistry(): ServerRegistry = error("Not used by the resolver")

    override fun getServers(): List<FakeServer> = servers
}
