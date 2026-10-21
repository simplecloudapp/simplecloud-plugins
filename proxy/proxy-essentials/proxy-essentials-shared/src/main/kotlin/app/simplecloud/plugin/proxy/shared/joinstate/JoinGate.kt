package app.simplecloud.plugin.proxy.shared.joinstate

import app.simplecloud.api.runtime.SimpleCloudRuntime
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.proxy.shared.config.JoinStateConfig
import app.simplecloud.plugin.proxy.shared.config.MessageConfig
import app.simplecloud.plugin.proxy.shared.config.ProxyEssentialsConfig
import app.simplecloud.plugin.proxy.shared.network.NetworkCache
import app.simplecloud.plugin.proxy.shared.platform.ProxyPlayer
import org.slf4j.LoggerFactory

class JoinGate(
    private val cache: NetworkCache,
    private val service: JoinStateService,
    private val config: ConfigurationFactory<ProxyEssentialsConfig>,
    private val messages: ConfigurationFactory<MessageConfig>,
) {

    private val logger = LoggerFactory.getLogger(JoinGate::class.java)

    fun checkProxyJoin(player: ProxyPlayer): JoinResult {
        if (config.get().whitelist.contains(player.getName(), player.getUniqueId())) return JoinResult.Allowed

        val kick = messages.get().kick
        val joinState = findJoinState(service.getLocalState()) ?: return JoinResult.Denied(kick.noJoinState)
        if (!hasJoinPermission(joinState, player)) {
            logger.info(
                "${player.getName()} is missing the permission '${joinState.permission.join}' to join the proxy",
            )
            return JoinResult.Denied(kick.noPermission)
        }

        if (isProxyFull() &&
            !player.hasPermission(joinState.permission.full)
        ) {
            return JoinResult.Denied(kick.networkFull)
        }
        return JoinResult.Allowed
    }

    fun checkServerSwitch(player: ProxyPlayer, serverName: String): JoinResult {
        if (config.get().whitelist.contains(player.getName(), player.getUniqueId())) return JoinResult.Allowed

        val kick = messages.get().kick
        val state = service.getServerState(serverName)
        if (state == null) {
            logger.warn(
                "The join states of the servers are not loaded yet, so ${player.getName()} cannot join '$serverName'",
            )
            return JoinResult.Denied(kick.noJoinState)
        }

        val joinState = findJoinState(state) ?: return JoinResult.Denied(kick.noJoinState)
        if (!hasJoinPermission(joinState, player)) {
            logger.info(
                "${player.getName()} is missing the permission '${joinState.permission.join}' to join '$serverName'",
            )
            return JoinResult.Denied(kick.noPermission)
        }

        return JoinResult.Allowed
    }

    private fun findJoinState(name: String): JoinStateConfig? {
        val joinState = config.get().joinstates.find { it.name == name }
        if (joinState == null) {
            logger.error(
                "The join state '$name' does not exist in config.yml, so nobody can join until it is added or the join state is changed.",
            )
        }
        return joinState
    }

    private fun hasJoinPermission(joinState: JoinStateConfig, player: ProxyPlayer): Boolean {
        val permission = joinState.permission.join
        return permission.isBlank() || player.hasPermission(permission)
    }

    private fun isProxyFull(): Boolean {
        val server = cache.getServer(SimpleCloudRuntime.serverId()) ?: return false
        val groupName = server.group?.name
        if (groupName == null) {
            val maxPlayers = server.maxPlayers ?: 0
            return maxPlayers in 1..(server.playerCount ?: 0)
        }

        val maxPlayers = cache.findGroup(groupName)?.maxPlayers ?: 0
        val onlinePlayers = cache.getServersOfGroup(groupName).sumOf { it.playerCount ?: 0 }
        return maxPlayers in 1..onlinePlayers
    }
}
