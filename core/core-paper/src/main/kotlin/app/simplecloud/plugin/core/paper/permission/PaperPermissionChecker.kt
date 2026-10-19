package app.simplecloud.plugin.core.paper.permission

import app.simplecloud.plugin.core.shared.permission.PermissionChecker
import org.bukkit.entity.Player

class PaperPermissionChecker : PermissionChecker<Player> {

    override fun checkPermission(player: Player, name: String): Boolean = player.hasPermission(name)
}
