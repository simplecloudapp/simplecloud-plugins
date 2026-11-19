package app.simplecloud.plugin.connection.shared

import app.simplecloud.api.CloudApi
import app.simplecloud.api.CloudApiOptions
import app.simplecloud.plugin.connection.shared.config.CommandConfig
import app.simplecloud.plugin.connection.shared.config.ConnectionConfig
import app.simplecloud.plugin.connection.shared.config.MessageConfig
import app.simplecloud.plugin.connection.shared.config.RegistrationServer
import app.simplecloud.plugin.connection.shared.connection.ConnectionResolver
import app.simplecloud.plugin.connection.shared.platform.ConnectionPlatform
import app.simplecloud.plugin.connection.shared.registration.RegisteredServer
import app.simplecloud.plugin.connection.shared.registration.ServerRegisterer
import app.simplecloud.plugin.connection.shared.registration.ServerRegistrations
import app.simplecloud.plugin.connection.shared.registration.ServerStatusCache
import app.simplecloud.plugin.core.shared.config.ConfigWatcher
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.core.shared.config.DefaultConfigInstaller
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

class ConnectionPlugin(
    platform: ConnectionPlatform,
) {

    private val api = CloudApi.create(CloudApiOptions.builder().component("server-connection").build())
    private val logger = LoggerFactory.getLogger(ConnectionPlugin::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val additionalServers = CopyOnWriteArrayList<RegisteredServer>()
    private val dir = platform.getDataDirectory()
    private val registry = platform.getRegistry()
    private val commandManager = platform.getCommandManager()
    private val configFiles = listOf("config.yml", "messages.yml", "commands.yml").map { name -> File(dir, name) }

    val connectionConfig = ConfigurationFactory.create(dir, "config.yml", ConnectionConfig())
    val messageConfig = ConfigurationFactory.create(dir, "messages.yml", MessageConfig())
    private val commandConfig = ConfigurationFactory.create(dir, "commands.yml", CommandConfig())

    private val registrationEnabled = connectionConfig.get().registration.enabled
    private val registrations = ServerRegistrations(registry)
    private val statusCache = ServerStatusCache(registrations)
    private val registerer = ServerRegisterer(api, registrations, statusCache, connectionConfig)

    val resolver = ConnectionResolver(platform, connectionConfig, messageConfig, statusCache)

    fun start() {
        validateConnections()
        commandManager.register(commandConfig.get().commands)
        ConfigWatcher(dir.toPath()) { reload() }.start(scope)
        if (!registrationEnabled) return

        registry.clear()
        registerAdditionalServers()
        logger.info("Starting server registration...")
        registerer.start()
    }

    fun shutdown() {
        commandManager.unregister()
        if (registrationEnabled) registerer.stop()
        scope.cancel()
        api.close()
    }

    private fun reload() {
        configFiles.forEach { file -> DefaultConfigInstaller.install(file.toPath(), javaClass.classLoader) }
        val emptyFile = configFiles.firstOrNull { file -> file.length() == 0L }
        if (emptyFile != null) {
            logger.warn("Skipping the reload because ${emptyFile.name} is empty")
            return
        }

        loadConfig("config.yml") { connectionConfig.loadOrCreate(ConnectionConfig()) }
        loadConfig("messages.yml") { messageConfig.loadOrCreate(MessageConfig()) }
        loadConfig("commands.yml") { commandConfig.loadOrCreate(CommandConfig()) }
        validateConnections()

        commandManager.unregister()
        commandManager.register(commandConfig.get().commands)

        if (connectionConfig.get().registration.enabled != registrationEnabled) {
            logger.warn("Changing registration.enabled only takes effect after a restart")
        }

        if (registrationEnabled) {
            registerAdditionalServers()
            scope.launch {
                registerer.sync()
            }
        }
    }

    private fun loadConfig(fileName: String, load: () -> Unit) {
        try {
            load()
        } catch (e: Exception) {
            logger.error("Failed to load $fileName, the previous version stays active", e)
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
            .filter { name -> resolver.findConnection(name, config.connections) == null }
            .forEach { name -> logger.warn("The connection '$name' is referenced but not defined in config.yml") }
    }

    private fun registerAdditionalServers() {
        val servers = connectionConfig.get().registration.additionalServers.map { server -> toRegisteredServer(server) }
        additionalServers
            .filter { server -> server !in servers }
            .forEach { server -> registry.unregister(server.proxyName, server) }

        servers
            .filter { server -> server !in additionalServers }
            .forEach { server -> logger.info("Additional server ${server.proxyName} has been registered!") }
        servers.forEach { server -> registry.register(server.proxyName, server) }
        additionalServers.clear()
        additionalServers.addAll(servers)
    }

    private fun toRegisteredServer(server: RegistrationServer): RegisteredServer = RegisteredServer(
        serverId = server.name,
        numericalId = 0,
        ip = server.address,
        port = server.port,
        serverBaseName = server.name,
        proxyName = server.name,
        properties = emptyMap(),
        persistent = true,
    )
}
