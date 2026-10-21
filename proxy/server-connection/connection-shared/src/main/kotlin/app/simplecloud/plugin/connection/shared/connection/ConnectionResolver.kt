package app.simplecloud.plugin.connection.shared.connection

import app.simplecloud.plugin.connection.shared.config.ConnectionConfig
import app.simplecloud.plugin.connection.shared.config.ConnectionEntry
import app.simplecloud.plugin.connection.shared.config.ConnectionRule
import app.simplecloud.plugin.connection.shared.config.FallbackTargetConnection
import app.simplecloud.plugin.connection.shared.config.RuleType
import app.simplecloud.plugin.connection.shared.config.SubdomainRoute
import app.simplecloud.plugin.connection.shared.platform.ConnectionPlatform
import app.simplecloud.plugin.connection.shared.platform.ConnectionPlayer
import app.simplecloud.plugin.connection.shared.platform.ConnectionServer

object ConnectionResolver {

    fun findConnection(name: String, connections: List<ConnectionEntry>): ConnectionEntry? = connections.find { it.name.equals(name, ignoreCase = true) }

    fun findRoute(host: String, routes: List<SubdomainRoute>): SubdomainRoute? = routes.find { route -> route.subdomain.equals(host, ignoreCase = true) }

    fun findMatchingServerNames(connection: ConnectionEntry, servers: List<String>): List<String> = servers.filter { connection.serverNameMatcher.matches(it) }

    fun checkRules(connection: ConnectionEntry, player: ConnectionPlayer): ConnectionRule? {
        for (rule in connection.rules) {
            if (rule.bypassPermission.isNotEmpty() && player.hasPermission(rule.bypassPermission)) {
                continue
            }

            val failed = when (rule.type) {
                RuleType.PERMISSION -> {
                    val hasPermission = player.hasPermission(rule.name)
                    val permissionRequired = rule.value.isEmpty() || rule.value.toBoolean()
                    hasPermission != permissionRequired
                }

                RuleType.ENV -> {
                    val envValue = System.getenv(rule.name) ?: ""
                    val matches = rule.operation.matches(envValue, rule.value, rule.negate)
                    !matches
                }
            }

            if (failed) return rule
        }
        return null
    }

    fun isServerInConnection(serverName: String, connectionName: String, connections: List<ConnectionEntry>, servers: List<String>): Boolean {
        val connection = findConnection(connectionName, connections) ?: return false
        val matchingNames = findMatchingServerNames(connection, servers)
        return matchingNames.any { it.equals(serverName, ignoreCase = true) }
    }

    fun <S : ConnectionServer> findJoinTarget(
        platform: ConnectionPlatform<S>,
        player: ConnectionPlayer,
        config: ConnectionConfig,
        stoppingServers: Set<String>,
    ): S? {
        val routeServer = findRouteTarget(platform, player, config, stoppingServers)
        if (routeServer != null || !config.networkJoinTargets.enabled) return routeServer

        return config.networkJoinTargets.targetConnections
            .sortedByDescending { it.priority }
            .asSequence()
            .mapNotNull { target -> findConnection(target.name, config.connections) }
            .firstNotNullOfOrNull { connection -> findLeastPopulated(platform, player, connection, null, stoppingServers) }
    }

    fun <S : ConnectionServer> findFallbackTarget(
        platform: ConnectionPlatform<S>,
        player: ConnectionPlayer,
        kickedServerName: String?,
        config: ConnectionConfig,
        stoppingServers: Set<String>,
    ): S? {
        val serverNames = platform.getServers().map { server -> server.getName() }
        return config.fallback.targetConnections
            .sortedByDescending { it.priority }
            .asSequence()
            .filter { target -> isFromAllowed(target, kickedServerName, config.connections, serverNames) }
            .mapNotNull { target -> findConnection(target.name, config.connections) }
            .firstNotNullOfOrNull { connection ->
                findLeastPopulated(platform, player, connection, kickedServerName, stoppingServers)
            }
    }

    fun <S : ConnectionServer> findCommandTarget(
        platform: ConnectionPlatform<S>,
        player: ConnectionPlayer,
        targets: List<FallbackTargetConnection>,
        config: ConnectionConfig,
        stoppingServers: Set<String>,
    ): CommandTarget<S> {
        val currentServerName = player.getServerName()
        val servers = platform.getServers()
        val serverNames = servers.map { server -> server.getName() }

        for (target in targets.sortedByDescending { it.priority }) {
            if (!isFromAllowed(target, currentServerName, config.connections, serverNames)) continue
            val connection = findConnection(target.name, config.connections) ?: continue
            if (checkRules(connection, player) != null) continue

            val matchingServers = servers.filter { server -> connection.serverNameMatcher.matches(server.getName()) }
            if (matchingServers.any { server -> server.getName().equals(currentServerName, ignoreCase = true) }) {
                return CommandTarget.AlreadyConnected
            }

            val server = matchingServers
                .filter { server -> server.getName() !in stoppingServers }
                .minByOrNull { server -> server.getPlayerCount() }
            if (server != null) return CommandTarget.Connect(server)
        }

        return CommandTarget.NotFound
    }

    private fun isFromAllowed(
        target: FallbackTargetConnection,
        fromServerName: String?,
        connections: List<ConnectionEntry>,
        serverNames: List<String>,
    ): Boolean = fromServerName == null ||
        target.from.isEmpty() ||
        target.from.any { isServerInConnection(fromServerName, it, connections, serverNames) }

    private fun <S : ConnectionServer> findRouteTarget(
        platform: ConnectionPlatform<S>,
        player: ConnectionPlayer,
        config: ConnectionConfig,
        stoppingServers: Set<String>,
    ): S? {
        val virtualHost = player.getVirtualHost() ?: return null
        val route = findRoute(virtualHost, config.address.routes) ?: return null
        val connection = findConnection(route.targetConnection, config.connections) ?: return null
        return findLeastPopulated(platform, player, connection, null, stoppingServers)
    }

    private fun <S : ConnectionServer> findLeastPopulated(
        platform: ConnectionPlatform<S>,
        player: ConnectionPlayer,
        connection: ConnectionEntry,
        excludedServerName: String?,
        stoppingServers: Set<String>,
    ): S? {
        if (checkRules(connection, player) != null) return null
        return platform.getServers()
            .filter { server -> connection.serverNameMatcher.matches(server.getName()) }
            .filter { server -> server.getName() !in stoppingServers }
            .filter { server -> !server.getName().equals(excludedServerName, ignoreCase = true) }
            .minByOrNull { server -> server.getPlayerCount() }
    }
}
