package app.simplecloud.plugin.proxy.shared.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SlotsConfigTest {

    @Test
    fun `disabled slots do not override the max players`() {
        val slots = SlotsConfig(enabled = false, type = MaxPlayerDisplayType.FAKE, fakeSlots = FAKE_SLOTS)

        val maxPlayers = slots.resolveMaxPlayersOverride(ONLINE_PLAYERS, COUNTED_MAX_PLAYERS)

        assertNull(maxPlayers)
    }

    @Test
    fun `real slots show the counted max players`() {
        val slots = SlotsConfig(type = MaxPlayerDisplayType.REAL)

        val maxPlayers = slots.resolveMaxPlayersOverride(ONLINE_PLAYERS, COUNTED_MAX_PLAYERS)

        assertEquals(COUNTED_MAX_PLAYERS, maxPlayers)
    }

    @Test
    fun `fake slots show the configured number`() {
        val slots = SlotsConfig(type = MaxPlayerDisplayType.FAKE, fakeSlots = FAKE_SLOTS)

        val maxPlayers = slots.resolveMaxPlayersOverride(ONLINE_PLAYERS, COUNTED_MAX_PLAYERS)

        assertEquals(FAKE_SLOTS, maxPlayers)
    }

    @Test
    fun `dynamic slots show the online players plus the configured range`() {
        val slots = SlotsConfig(type = MaxPlayerDisplayType.DYNAMIC, dynamicPlayerRange = DYNAMIC_PLAYER_RANGE)

        val maxPlayers = slots.resolveMaxPlayersOverride(ONLINE_PLAYERS, COUNTED_MAX_PLAYERS)

        assertEquals(ONLINE_PLAYERS + DYNAMIC_PLAYER_RANGE, maxPlayers)
    }

    private companion object {
        const val ONLINE_PLAYERS = 12
        const val COUNTED_MAX_PLAYERS = 250
        const val FAKE_SLOTS = 40
        const val DYNAMIC_PLAYER_RANGE = 3
    }
}
