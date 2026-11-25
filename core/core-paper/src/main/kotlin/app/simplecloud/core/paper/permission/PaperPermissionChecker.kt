package app.simplecloud.core.paper.permission

import org.bukkit.entity.Player

class PaperPermissionChecker : app.simplecloud.core.shared.permission.PermissionChecker<Player> {

    override fun checkPermission(player: Player, name: String): Boolean = player.hasPermission(name)
}
