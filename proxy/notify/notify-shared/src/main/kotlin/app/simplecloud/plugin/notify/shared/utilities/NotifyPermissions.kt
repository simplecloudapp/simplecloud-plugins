package app.simplecloud.plugin.notify.shared.utilities

object NotifyPermissions {

    const val SERVER_STARTING = "notify.state.server.starting"
    const val SERVER_AVAILABLE = "notify.state.server.available"
    const val SERVER_STOPPING = "notify.state.server.stopping"
    const val SERVER_STOPPED = "notify.state.server.stopped"

    const val GROUP_CREATED = "notify.state.group.created"
    const val GROUP_DELETED = "notify.state.group.deleted"

    const val PERSISTENT_SERVER_CREATED = "notify.state.persistent-server.created"
    const val PERSISTENT_SERVER_DELETED = "notify.state.persistent-server.deleted"

    const val COMMAND_HELP = "simplecloud.command.notify.help"
    const val COMMAND_RELOAD_CONFIG = "simplecloud.command.notify.reload.config"
    const val COMMAND_TOGGLE_SELF = "simplecloud.command.notify.toggle.self"
    const val COMMAND_TOGGLE_OTHER = "simplecloud.command.notify.toggle.other"

}
