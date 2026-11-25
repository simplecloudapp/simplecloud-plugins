package app.simplecloud.core.velocity.permission

import app.simplecloud.core.shared.permission.PermissionChecker
import com.velocitypowered.api.proxy.Player

class VelocityPermissionChecker : PermissionChecker<Player> {

    override fun checkPermission(player: Player, name: String): Boolean = player.hasPermission(name)
}
