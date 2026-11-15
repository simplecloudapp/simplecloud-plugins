package app.simplecloud.plugin.proxy.shared.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SlotsConfigTest {

    @Test
    fun `disabled slots do not override the max players`() {
        val slots = SlotsConfig(enabled = false, type = MaxPlayerDisplayType.FAKE)

        val maxPlayers = slots.resolveMaxPlayersOverride(ONLINE_PLAYERS, COUNTED_MAX_PLAYERS)

        assertNull(maxPlayers)
    }

    @Test
    fun `enabled slots override the max players by their type`() {
        val real = SlotsConfig(type = MaxPlayerDisplayType.REAL)
        val fake = SlotsConfig(type = MaxPlayerDisplayType.FAKE, fakeSlots = 100)
        val dynamic = SlotsConfig(type = MaxPlayerDisplayType.DYNAMIC, dynamicPlayerRange = 5)

        assertEquals(COUNTED_MAX_PLAYERS, real.resolveMaxPlayersOverride(ONLINE_PLAYERS, COUNTED_MAX_PLAYERS))
        assertEquals(100, fake.resolveMaxPlayersOverride(ONLINE_PLAYERS, COUNTED_MAX_PLAYERS))
        assertEquals(ONLINE_PLAYERS + 5, dynamic.resolveMaxPlayersOverride(ONLINE_PLAYERS, COUNTED_MAX_PLAYERS))
    }

    private companion object {
        const val ONLINE_PLAYERS = 12
        const val COUNTED_MAX_PLAYERS = 250
    }
}
