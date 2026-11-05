package app.simplecloud.plugin.proxy.shared.tablist

import app.simplecloud.plugin.core.shared.matcher.OperationType
import app.simplecloud.plugin.core.shared.matcher.ServerMatcherConfiguration
import app.simplecloud.plugin.proxy.shared.config.TabListGroup
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TabListGroupResolverTest {

    private val global = TabListGroup(name = "global")
    private val lobby = TabListGroup(name = "lobby")
    private val bedwars = TabListGroup(
        name = "minigames",
        serverNameMatcher = ServerMatcherConfiguration(OperationType.CONTAINS, "bedwars"),
    )

    @Test
    fun `matcher wins over name`() {
        val groups = listOf(global, lobby, bedwars)

        assertEquals(bedwars, TabListGroupResolver.resolve(groups, "lobby-bedwars-1"))
    }

    @Test
    fun `name is still used when no matcher matches`() {
        val groups = listOf(global, lobby, bedwars)

        assertEquals(lobby, TabListGroupResolver.resolve(groups, "lobby-1"))
        assertEquals(global, TabListGroupResolver.resolve(groups, "skywars-1"))
    }

    @Test
    fun `group with a matcher is not matched by its name`() {
        val groups = listOf(bedwars)

        assertNull(TabListGroupResolver.resolve(groups, "minigames-1"))
    }
}
