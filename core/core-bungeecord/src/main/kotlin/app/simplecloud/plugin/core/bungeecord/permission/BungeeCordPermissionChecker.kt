package app.simplecloud.plugin.core.bungeecord.permission

import app.simplecloud.plugin.core.shared.permission.PermissionChecker
import net.md_5.bungee.api.connection.ProxiedPlayer

class BungeeCordPermissionChecker : PermissionChecker<ProxiedPlayer> {

    override fun checkPermission(player: ProxiedPlayer, name: String): Boolean = player.hasPermission(name)
}
