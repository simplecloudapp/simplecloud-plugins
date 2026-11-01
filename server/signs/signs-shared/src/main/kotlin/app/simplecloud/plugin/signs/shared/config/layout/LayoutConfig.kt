package app.simplecloud.plugin.signs.shared.config.layout

import app.simplecloud.api.server.Server
import app.simplecloud.plugin.signs.shared.rule.SignRule
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class LayoutConfig(
    val name: String = "",
    val type: LayoutType = LayoutType.ALL,
    val matcher: Map<MatcherType, @JvmSuppressWildcards List<MatcherEntry>> = emptyMap(),
    val rule: String = "EMPTY",
    val priority: Int = 0,
    val serverName: String = "%server-name%",
    val behindBlock: String? = null,
    val glowingText: Boolean = false,
    val frameUpdateInterval: Long = 500,
    val frames: List<FrameConfig> = emptyList(),
) {

    fun appliesTo(server: Server?): Boolean = when (type) {
        LayoutType.ALL -> true
        LayoutType.GROUP -> server?.isFromGroup == true
        LayoutType.PERSISTENT -> server?.isFromPersistentServer == true
    }

    fun ruleMatches(server: Server?): Boolean = SignRule.entries.any { it.name == rule && it.matches(server) }

    fun nameMatches(serverName: String): Boolean {
        val all = matcher[MatcherType.MATCH_ALL]
        if (all != null && !all.all { it.operation.matches(serverName, it.value) }) return false
        val any = matcher[MatcherType.MATCH_ANY] ?: return true
        return any.any { it.operation.matches(serverName, it.value) }
    }
}

@ConfigSerializable
data class MatcherEntry(
    val operation: MatcherOperation = MatcherOperation.STARTS_WITH,
    val value: String = "",
)

@ConfigSerializable
data class FrameConfig(
    val lines: List<String> = emptyList(),
)
