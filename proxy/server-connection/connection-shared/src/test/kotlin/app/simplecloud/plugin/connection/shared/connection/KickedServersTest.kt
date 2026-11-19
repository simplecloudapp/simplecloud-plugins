package app.simplecloud.plugin.connection.shared.connection

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KickedServersTest {

    private val kickedServers = KickedServers()
    private val playerId = UUID.randomUUID()
    private val otherPlayerId = UUID.randomUUID()

    @Test
    fun `returns every server that kicked the player`() {
        kickedServers.recordKick(playerId, "lobby-1")

        val kickedServerNames = kickedServers.recordKick(playerId, "lobby-2")

        assertEquals(setOf("lobby-1", "lobby-2"), kickedServerNames)
    }

    @Test
    fun `forgets the kicks of a cleared player`() {
        kickedServers.recordKick(playerId, "lobby-1")
        kickedServers.clear(playerId)

        val kickedServerNames = kickedServers.recordKick(playerId, "lobby-2")

        assertEquals(setOf("lobby-2"), kickedServerNames)
    }

    @Test
    fun `knows whether a player has kicks`() {
        kickedServers.recordKick(playerId, "lobby-1")

        assertTrue(kickedServers.hasKicks(playerId))
        assertFalse(kickedServers.hasKicks(otherPlayerId))
    }

    @Test
    fun `keeps the kicks of each player apart`() {
        kickedServers.recordKick(otherPlayerId, "lobby-1")

        val kickedServerNames = kickedServers.recordKick(playerId, "lobby-2")

        assertEquals(setOf("lobby-2"), kickedServerNames)
    }
}
