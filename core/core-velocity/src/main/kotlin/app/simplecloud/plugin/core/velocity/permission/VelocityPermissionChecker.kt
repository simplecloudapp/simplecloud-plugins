package app.simplecloud.plugin.core.velocity.permission

import app.simplecloud.plugin.core.shared.permission.PermissionChecker
import com.velocitypowered.api.proxy.Player

class VelocityPermissionChecker : PermissionChecker<Player> {

    override fun checkPermission(player: Player, name: String): Boolean = player.hasPermission(name)
}
