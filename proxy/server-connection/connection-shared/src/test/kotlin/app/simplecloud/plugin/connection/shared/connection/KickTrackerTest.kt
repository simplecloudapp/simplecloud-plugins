package app.simplecloud.plugin.connection.shared.connection

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class KickTrackerTest {

    private val kickTracker = KickTracker()
    private val playerId = UUID.randomUUID()

    @Test
    fun `returns every server that kicked the player recently`() {
        assertEquals(setOf("lobby-1"), kickTracker.add(playerId, "lobby-1"))
        assertEquals(setOf("lobby-1", "lobby-2"), kickTracker.add(playerId, "lobby-2"))
    }

    @Test
    fun `kicks are tracked per player`() {
        kickTracker.add(playerId, "lobby-1")

        assertEquals(setOf("lobby-2"), kickTracker.add(UUID.randomUUID(), "lobby-2"))
    }

    @Test
    fun `removing a player forgets their kicks`() {
        kickTracker.add(playerId, "lobby-1")

        kickTracker.remove(playerId)

        assertEquals(setOf("lobby-2"), kickTracker.add(playerId, "lobby-2"))
    }
}
