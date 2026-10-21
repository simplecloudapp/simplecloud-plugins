package app.simplecloud.plugin.connection.shared

import app.simplecloud.api.CloudApi
import app.simplecloud.plugin.connection.shared.config.CommandConfig
import app.simplecloud.plugin.connection.shared.config.ConnectionConfig
import app.simplecloud.plugin.connection.shared.config.MessageConfig
import app.simplecloud.plugin.connection.shared.config.RegistrationServer
import app.simplecloud.plugin.connection.shared.connection.ConnectionResolver
import app.simplecloud.plugin.connection.shared.registration.RegisteredServer
import app.simplecloud.plugin.connection.shared.registration.ServerRegisterer
import app.simplecloud.plugin.connection.shared.registration.ServerRegistry
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

class ConnectionPlugin(
    private val dir: File,
    private val registry: ServerRegistry,
) {

    private val api = CloudApi.create()
    private val logger = LoggerFactory.getLogger(ConnectionPlugin::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val additionalServers = CopyOnWriteArrayList<RegisteredServer>()

    val connectionConfig = ConfigurationFactory.create(dir, "config.yml", ConnectionConfig())
    val messageConfig = ConfigurationFactory.create(dir, "messages.yml", MessageConfig())
    val commandConfig = ConfigurationFactory.create(dir, "commands.yml", CommandConfig())

    private val registrationEnabled = connectionConfig.get().registration.enabled
    private val registerer = ServerRegisterer(api, registry, connectionConfig)

    fun start() {
        logger.info("SimpleCloud v3 connection plugin initialized!")
        validateConnections()
        if (!registrationEnabled) return

        registerAdditionalServers()
        scope.launch {
            startRegistration()
        }
    }

    fun shutdown() {
        logger.info("SimpleCloud v3 connection plugin uninitialized!")
        if (registrationEnabled) {
            logger.info("Stopping server registration...")
            registerer.stop()
        }
        scope.cancel()
        api.close()
    }

    fun isStopping(serverName: String): Boolean = registerer.isStopping(serverName)

    fun reload() {
        connectionConfig.loadOrCreate(ConnectionConfig())
        messageConfig.loadOrCreate(MessageConfig())
        commandConfig.loadOrCreate(CommandConfig())
        validateConnections()

        if (connectionConfig.get().registration.enabled != registrationEnabled) {
            logger.warn("Changing registration.enabled only takes effect after a restart")
        }

        if (registrationEnabled) {
            unregisterAdditionalServers()
            registerAdditionalServers()
            scope.launch {
                registerer.refresh()
            }
        }
    }

    private fun validateConnections() {
        val config = connectionConfig.get()
        val referencedNames = config.address.routes.map { route -> route.targetConnection } +
            config.networkJoinTargets.targetConnections.map { target -> target.name } +
            config.fallback.targetConnections.flatMap { target -> target.from + target.name } +
            commandConfig.get().commands.flatMap { command ->
                command.targetConnections.flatMap { target -> target.from + target.name }
            }

        referencedNames
            .distinctBy { name -> name.lowercase() }
            .filter { name -> ConnectionResolver.findConnection(name, config.connections) == null }
            .forEach { name -> logger.warn("The connection '$name' is referenced but not defined in config.yml") }
    }

    private suspend fun startRegistration() {
        logger.info("Starting server registration...")
        registerer.start()
        registerer.loadExistingServers()
    }

    private fun registerAdditionalServers() {
        connectionConfig.get().registration.additionalServers.forEach { additionalServer ->
            val server = toRegisteredServer(additionalServer)
            registry.register(server.proxyName, server)
            additionalServers.add(server)
            logger.info("Additional server ${server.proxyName} has been registered!")
        }
    }

    private fun unregisterAdditionalServers() {
        additionalServers.forEach { server -> registry.unregister(server.proxyName, server) }
        additionalServers.clear()
    }

    private fun toRegisteredServer(server: RegistrationServer): RegisteredServer = RegisteredServer(
        serverId = server.name,
        numericalId = 0,
        ip = server.address,
        port = server.port,
        serverBaseName = server.name,
        proxyName = server.name,
        properties = emptyMap(),
        blueprintConfigurator = null,
        persistent = true,
    )
}
