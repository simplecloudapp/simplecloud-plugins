package app.simplecloud.plugin.api.paper.permission

import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import org.bukkit.entity.Player

class PaperPermissionChecker : PermissionChecker<Player> {

    override fun checkPermission(player: Player, name: String): Boolean {
        return player.hasPermission(name)
    }

}
