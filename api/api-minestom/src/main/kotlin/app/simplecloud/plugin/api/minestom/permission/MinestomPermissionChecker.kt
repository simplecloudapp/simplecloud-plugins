package app.simplecloud.plugin.api.minestom.permission

import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import net.minestom.server.entity.Player

class MinestomPermissionChecker(private val permissions: MinestomPermissions) : PermissionChecker<Player> {

    override fun checkPermission(player: Player, name: String): Boolean {
        return permissions.hasPermission(player, name)
    }

}
