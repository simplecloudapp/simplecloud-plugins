package app.simplecloud.plugin.api.bungeecord.permission

import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import net.md_5.bungee.api.connection.ProxiedPlayer

class BungeeCordPermissionChecker : PermissionChecker<ProxiedPlayer> {

    override fun checkPermission(player: ProxiedPlayer, name: String): Boolean {
        return player.hasPermission(name)
    }

}
