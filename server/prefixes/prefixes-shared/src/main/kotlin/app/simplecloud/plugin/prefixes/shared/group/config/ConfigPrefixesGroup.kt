package app.simplecloud.plugin.prefixes.shared.group.config

import app.simplecloud.plugin.core.shared.adventure.miniMessage
import app.simplecloud.plugin.core.shared.permission.PermissionChecker
import app.simplecloud.plugin.prefixes.api.group.PrefixesGroup
import app.simplecloud.plugin.prefixes.shared.config.ConfigGroup
import app.simplecloud.plugin.prefixes.shared.utilities.ColorParser
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import java.util.UUID
import java.util.concurrent.CompletableFuture

class ConfigPrefixesGroup(
    group: ConfigGroup,
    private val permissionChecker: PermissionChecker<UUID>,
) : PrefixesGroup {

    override val name: String = group.name
    override val priority: Int = group.priority
    override val permission: String = group.permission
    override val prefix: Component = miniMessage.deserialize(group.prefix)
    override val suffix: Component = miniMessage.deserialize(group.suffix)
    override val color: TextColor = ColorParser.parse(group.color) ?: NamedTextColor.WHITE
    override val displayName: String = group.displayName
    override val chatFormat: String = group.chatFormat

    override fun containsPlayer(player: UUID): CompletableFuture<Boolean> = CompletableFuture.completedFuture(hasPermission(player))

    fun hasPermission(player: UUID): Boolean {
        if (permission.isEmpty()) return true
        return permissionChecker.checkPermission(player, permission)
    }
}
