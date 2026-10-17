package app.simplecloud.plugin.notify.shared.notification

import app.simplecloud.api.player.PlayerApi
import kotlinx.coroutines.future.await
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class NotificationCache(
    private val api: PlayerApi,
) {

    private val disabledTypes: MutableMap<UUID, Set<NotificationType>> = ConcurrentHashMap()

    suspend fun load(id: UUID) {
        disabledTypes[id] = fetchDisabledTypes(id)
    }

    fun unload(id: UUID) {
        disabledTypes.remove(id)
    }

    fun isEnabled(id: UUID, type: NotificationType): Boolean {
        val disabled = disabledTypes[id] ?: return true
        return type !in disabled
    }

    suspend fun fetchDisabledTypes(id: UUID): Set<NotificationType> {
        val properties = api.get(id).await()?.properties ?: emptyMap()

        return NotificationType.entries
            .filter { type -> properties[propertyOf(type)]?.toBooleanStrictOrNull() == false }
            .toSet()
    }

    suspend fun setEnabled(id: UUID, types: Collection<NotificationType>, value: Boolean) {
        api.updatePlayerProperties(id, types.associate { type -> propertyOf(type) to value.toString() }).await()
        disabledTypes.computeIfPresent(id) { _, disabled -> if (value) disabled - types.toSet() else disabled + types }
    }

    fun clear() {
        disabledTypes.clear()
    }

    private fun propertyOf(type: NotificationType): String {
        return "notify.${type.key}.enabled"
    }

}
