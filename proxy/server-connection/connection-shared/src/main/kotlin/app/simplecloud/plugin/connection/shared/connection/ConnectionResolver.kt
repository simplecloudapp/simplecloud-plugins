package app.simplecloud.plugin.connection.shared.connection

import app.simplecloud.plugin.connection.shared.config.CommandEntry
import app.simplecloud.plugin.connection.shared.config.ConnectionConfig
import app.simplecloud.plugin.connection.shared.config.ConnectionEntry
import app.simplecloud.plugin.connection.shared.config.ConnectionRule
import app.simplecloud.plugin.connection.shared.config.FallbackTargetConnection
import app.simplecloud.plugin.connection.shared.config.MessageConfig
import app.simplecloud.plugin.connection.shared.config.RuleType
import app.simplecloud.plugin.connection.shared.platform.ConnectionPlatform
import app.simplecloud.plugin.connection.shared.platform.ConnectionPlayer
import app.simplecloud.plugin.connection.shared.platform.ConnectionServer
import app.simplecloud.plugin.connection.shared.registration.ServerStatus
import app.simplecloud.plugin.connection.shared.registration.ServerStatusCache
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

class ConnectionResolver(
    private val platform: ConnectionPlatform,
    private val connectionConfig: ConfigurationFactory<ConnectionConfig>,
    private val messageConfig: ConfigurationFactory<MessageConfig>,
    private val statusCache: ServerStatusCache,
) {

    fun findConnection(name: String, connections: List<ConnectionEntry>): ConnectionEntry? =
        connections.find { connection -> connection.name.equals(name, ignoreCase = true) }

    fun findJoinTarget(player: ConnectionPlayer, excludedServerNames: Set<String> = emptySet()): String? {
        val config = connectionConfig.get()
        val servers = findAvailableServers(excludedServerNames)
        val routeServerName = findRouteTarget(player, config, servers)
        if (routeServerName != null || !config.networkJoinTargets.enabled) return routeServerName

        return config.networkJoinTargets.targetConnections
            .sortedByDescending { target -> target.priority }
            .asSequence()
            .mapNotNull { target -> findConnection(target.name, config.connections) }
            .filter { connection -> passesRules(connection, player) }
            .firstNotNullOfOrNull { connection -> findLeastPopulated(connection, servers) }
    }

    fun findFallbackTarget(
        player: ConnectionPlayer,
        kickedServerName: String?,
        excludedServerNames: Set<String> = emptySet(),
    ): String? {
        val config = connectionConfig.get()
        val servers = findAvailableServers(excludedServerNames + listOfNotNull(kickedServerName))

        return config.fallback.targetConnections
            .sortedByDescending { target -> target.priority }
            .asSequence()
            .filter { target -> isFromAllowed(target, kickedServerName, config.connections) }
            .mapNotNull { target -> findConnection(target.name, config.connections) }
            .filter { connection -> passesRules(connection, player) }
            .firstNotNullOfOrNull { connection -> findLeastPopulated(connection, servers) }
    }

    fun executeCommand(player: ConnectionPlayer, command: CommandEntry) {
        val messages = messageConfig.get()
        when (val target = findCommandTarget(player, command.targetConnections)) {
            is ConnectionTarget.Connect -> player.connect(target.serverName)

            ConnectionTarget.AlreadyConnected -> {
                val server = Placeholder.unparsed("server", player.getServerName().orEmpty())
                player.sendMessage(messages.msg(command.messages.alreadyConnected, server))
            }

            ConnectionTarget.NotFound -> player.sendMessage(messages.msg(command.messages.noTargetConnectionFound))
        }
    }

    private fun findCommandTarget(player: ConnectionPlayer, targets: List<FallbackTargetConnection>): ConnectionTarget {
        val config = connectionConfig.get()
        val currentServerName = player.getServerName()
        val currentServerStopping = statusCache.findAllByProxyName()[currentServerName]?.stopping == true
        val servers = findAvailableServers(emptySet())

        for (target in targets.sortedByDescending { target -> target.priority }) {
            if (!isFromAllowed(target, currentServerName, config.connections)) continue
            val connection = findConnection(target.name, config.connections) ?: continue
            if (!passesRules(connection, player)) continue
            if (currentServerName != null && !currentServerStopping &&
                connection.serverNameMatcher.matches(currentServerName)
            ) {
                return ConnectionTarget.AlreadyConnected
            }

            val serverName = findLeastPopulated(connection, servers)
            if (serverName != null) return ConnectionTarget.Connect(serverName)
        }

        return ConnectionTarget.NotFound
    }

    private fun findRouteTarget(
        player: ConnectionPlayer,
        config: ConnectionConfig,
        servers: List<ConnectionServer>,
    ): String? {
        val virtualHost = player.getVirtualHost() ?: return null
        val route = config.address.routes.find { route -> route.subdomain.equals(virtualHost, ignoreCase = true) }
            ?: return null
        val connection = findConnection(route.targetConnection, config.connections) ?: return null
        if (!passesRules(connection, player)) return null
        return findLeastPopulated(connection, servers)
    }

    private fun findLeastPopulated(connection: ConnectionEntry, servers: List<ConnectionServer>): String? = servers
        .firstOrNull { server -> connection.serverNameMatcher.matches(server.name) }
        ?.name

    private fun findAvailableServers(excludedServerNames: Set<String>): List<ConnectionServer> {
        val statuses = statusCache.findAllByProxyName()

        return platform.getServers()
            .filter { server -> excludedServerNames.none { name -> name.equals(server.name, ignoreCase = true) } }
            .filter { server -> statuses[server.name]?.stopping != true }
            .map { server ->
                val cloudPlayerCount = statuses[server.name]?.playerCount ?: 0
                server.copy(playerCount = maxOf(server.playerCount, cloudPlayerCount))
            }
            .sortedWith(
                compareBy(
                    { server -> !hasFreeSlot(server, statuses[server.name]) },
                    { server -> server.playerCount },
                ),
            )
    }

    private fun hasFreeSlot(server: ConnectionServer, status: ServerStatus?): Boolean {
        val maxPlayers = status?.maxPlayers ?: 0
        return maxPlayers <= 0 || server.playerCount < maxPlayers
    }

    private fun isFromAllowed(
        target: FallbackTargetConnection,
        fromServerName: String?,
        connections: List<ConnectionEntry>,
    ): Boolean {
        if (fromServerName == null || target.from.isEmpty()) return true
        return target.from
            .mapNotNull { connectionName -> findConnection(connectionName, connections) }
            .any { connection -> connection.serverNameMatcher.matches(fromServerName) }
    }

    private fun passesRules(connection: ConnectionEntry, player: ConnectionPlayer): Boolean =
        connection.rules.all { rule -> passesRule(rule, player) }

    private fun passesRule(rule: ConnectionRule, player: ConnectionPlayer): Boolean {
        if (rule.bypassPermission.isNotEmpty() && player.hasPermission(rule.bypassPermission)) return true

        return when (rule.type) {
            RuleType.PERMISSION -> {
                val permissionRequired = rule.value.isEmpty() || rule.value.toBoolean()
                player.hasPermission(rule.name) == permissionRequired
            }

            RuleType.ENV -> rule.operation.matches(System.getenv(rule.name).orEmpty(), rule.value, rule.negate)
        }
    }
}
