package app.simplecloud.plugin.connection.shared.registration

data class ServerStatus(
    val stopping: Boolean,
    val playerCount: Int,
    val maxPlayers: Int,
)
