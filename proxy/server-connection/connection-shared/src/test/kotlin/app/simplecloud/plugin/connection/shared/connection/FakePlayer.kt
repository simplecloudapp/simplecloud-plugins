package app.simplecloud.plugin.connection.shared.connection

import app.simplecloud.plugin.connection.shared.platform.ConnectionPlayer
import java.util.UUID

object FakePlayer : ConnectionPlayer {

    private val uniqueId = UUID.randomUUID()

    override fun getUniqueId(): UUID = uniqueId

    override fun hasPermission(permission: String): Boolean = false

    override fun getVirtualHost(): String? = null

    override fun getServerName(): String? = null
}
