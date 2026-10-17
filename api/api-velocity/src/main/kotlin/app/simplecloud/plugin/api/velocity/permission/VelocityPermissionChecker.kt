package app.simplecloud.plugin.api.velocity.permission

import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import com.velocitypowered.api.proxy.Player

/**
 * @author Niklas Nieberler
 */

class VelocityPermissionChecker : PermissionChecker<Player> {

    override fun checkPermission(player: Player, name: String): Boolean {
        return player.hasPermission(name)
    }

}