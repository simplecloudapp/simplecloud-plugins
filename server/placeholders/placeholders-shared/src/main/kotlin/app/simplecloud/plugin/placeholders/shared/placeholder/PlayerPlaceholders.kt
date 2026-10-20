package app.simplecloud.plugin.placeholders.shared.placeholder

import app.simplecloud.api.CloudApi
import app.simplecloud.api.player.CloudPlayer
import kotlinx.coroutines.future.await
import java.util.UUID

object PlayerPlaceholders : PlaceholderType<CloudPlayer> {

    override val prefix = "player"

    override val properties = setOf(
        "uuid",
        "name",
        "display_name",
        "server",
        "proxy",
        "online",
        "online_time",
        "online_time_seconds",
        "first_seen",
        "last_seen",
    )

    override fun getValue(resource: CloudPlayer, property: String): Any? = when (property) {
        "uuid" -> resource.uniqueId
        "name" -> resource.name
        "display_name" -> resource.displayName
        "server" -> resource.connectedServerName
        "proxy" -> resource.connectedProxyName
        "online" -> resource.isOnline
        "online_time" -> formatDuration(resource.onlineTimeSeconds)
        "online_time_seconds" -> resource.onlineTimeSeconds
        "first_seen" -> resource.firstSeen
        "last_seen" -> resource.lastSeen
        else -> null
    }

    override fun isViewerScoped() = true

    override suspend fun loadCurrent(api: CloudApi): CloudPlayer? = null

    override suspend fun load(api: CloudApi, target: String): CloudPlayer? {
        if (target.isBlank()) return null
        val uniqueId = parseUniqueId(target)
        if (uniqueId != null) return api.player().find(uniqueId).await().orElse(null)
        return api.player().find(target).await().orElse(null)
    }

    private fun parseUniqueId(value: String): UUID? = try {
        UUID.fromString(value)
    } catch (_: IllegalArgumentException) {
        null
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = seconds % 3600 / 60
        return "${hours}h ${minutes}m"
    }
}
