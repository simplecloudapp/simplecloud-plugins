package app.simplecloud.plugin.proxy.shared.config

import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.core.shared.matcher.OperationType
import app.simplecloud.plugin.core.shared.matcher.ServerMatcherConfiguration
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TabListConfigTest {

    @Test
    fun `bundled config loads the commented server name matcher once enabled`() {
        val bundled = javaClass.classLoader.getResource("config/config.yml")?.readText() ?: error("config.yml missing")
        val enabled = bundled
            .replace("    # server-name-matcher:", "    server-name-matcher:")
            .replace("    #   operation: STARTS_WITH", "      operation: STARTS_WITH")
            .replace("    #   value: lobby", "      value: lobby")
            .replace("    #   negate: false", "      negate: false")

        val defaultGroup = load(bundled).tablist.single()
        val matcherGroup = load(enabled).tablist.single()

        assertNull(defaultGroup.serverNameMatcher)
        assertEquals(ServerMatcherConfiguration(OperationType.STARTS_WITH, "lobby"), matcherGroup.serverNameMatcher)
    }

    private fun load(yaml: String): ProxyEssentialsConfig {
        val file = Files.createTempFile("proxy-essentials", ".yml").toFile()
        file.deleteOnExit()
        file.writeText(yaml)
        val factory = ConfigurationFactory(file, ProxyEssentialsConfig::class.java)
        factory.reload()
        return factory.get()
    }
}
