package app.simplecloud.core.minestom.permission

import net.minestom.server.entity.Player

class MinestomPermissionChecker(private val permissions: MinestomPermissions) :
    app.simplecloud.core.shared.permission.PermissionChecker<Player> {

    override fun checkPermission(player: Player, name: String): Boolean = permissions.hasPermission(player, name)
}
