package app.simplecloud.plugin.core.minestom.permission

import app.simplecloud.plugin.core.shared.permission.PermissionChecker
import net.minestom.server.entity.Player

class MinestomPermissionChecker(private val permissions: MinestomPermissions) : PermissionChecker<Player> {

    override fun checkPermission(player: Player, name: String): Boolean = permissions.hasPermission(player, name)
}
