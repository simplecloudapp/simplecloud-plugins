package app.simplecloud.plugin.connection.shared.registration

data class RegisteredServer(
    val serverId: String,
    val numericalId: Int,
    val ip: String,
    val port: Int,
    val serverBaseName: String,
    val proxyName: String,
    val properties: Map<String, Any>,
    val persistent: Boolean,
)
