package app.simplecloud.plugin.connection.shared.utilities

import app.simplecloud.plugin.connection.shared.config.RegistrationConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class RegisteredServerResolverTest {

    private val config = RegistrationConfig()

    @Test
    fun `dynamic servers use the server name pattern`() {
        assertEquals("lobby-3", resolve(numericalId = 3, serverBaseName = "lobby", persistent = false))
    }

    @Test
    fun `persistent servers use the persistent server name pattern`() {
        assertEquals("survival", resolve(numericalId = 1, serverBaseName = "survival", persistent = true))
    }

    @Test
    fun `the id placeholder is replaced with the server id`() {
        val name = resolve(
            numericalId = 1,
            serverBaseName = "lobby",
            persistent = false,
            config = config.copy(serverNamePattern = "<group>-<id>"),
        )

        assertEquals("lobby-server-id", name)
    }

    @Test
    fun `properties are available as placeholders with normalized names`() {
        val name = resolve(
            numericalId = 1,
            serverBaseName = "lobby",
            persistent = false,
            config = config.copy(serverNamePattern = "<proxy_name>"),
            properties = mapOf("Proxy-Name" to "hub"),
        )

        assertEquals("hub", name)
    }

    private fun resolve(
        numericalId: Int,
        serverBaseName: String,
        persistent: Boolean,
        config: RegistrationConfig = this.config,
        properties: Map<String, Any> = emptyMap(),
    ): String = RegisteredServerResolver.resolve(
        serverId = "server-id",
        numericalId = numericalId,
        serverBaseName = serverBaseName,
        properties = properties,
        persistent = persistent,
        config = config,
    )
}
