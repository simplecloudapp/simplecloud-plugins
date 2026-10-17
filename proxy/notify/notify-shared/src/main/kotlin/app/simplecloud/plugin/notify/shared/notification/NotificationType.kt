package app.simplecloud.plugin.notify.shared.notification

import app.simplecloud.api.server.ServerState
import app.simplecloud.plugin.notify.shared.command.CloudSender
import app.simplecloud.plugin.notify.shared.utilities.NotifyPermissions

enum class NotificationType(
    val key: String,
    val permission: String
) {

    SERVER_STARTING("server.starting", NotifyPermissions.SERVER_STARTING),
    SERVER_AVAILABLE("server.available", NotifyPermissions.SERVER_AVAILABLE),
    SERVER_STOPPING("server.stopping", NotifyPermissions.SERVER_STOPPING),
    SERVER_STOPPED("server.stopped", NotifyPermissions.SERVER_STOPPED),
    GROUP_CREATED("group.created", NotifyPermissions.GROUP_CREATED),
    GROUP_DELETED("group.deleted", NotifyPermissions.GROUP_DELETED),
    PERSISTENT_SERVER_CREATED("persistent-server.created", NotifyPermissions.PERSISTENT_SERVER_CREATED),
    PERSISTENT_SERVER_DELETED("persistent-server.deleted", NotifyPermissions.PERSISTENT_SERVER_DELETED);

    fun canReceive(sender: CloudSender): Boolean {
        return sender.hasPermission(permission)
    }

    companion object {
        fun fromKey(key: String): NotificationType? {
            return entries.firstOrNull { type -> type.key.equals(key, ignoreCase = true) }
        }

        fun fromServerState(state: ServerState): NotificationType? {
            return when (state) {
                ServerState.STARTING -> SERVER_STARTING
                ServerState.AVAILABLE -> SERVER_AVAILABLE
                ServerState.STOPPING -> SERVER_STOPPING
                else -> null
            }
        }
    }
}
