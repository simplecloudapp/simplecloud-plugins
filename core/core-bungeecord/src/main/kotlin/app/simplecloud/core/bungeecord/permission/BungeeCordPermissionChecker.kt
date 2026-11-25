package app.simplecloud.core.bungeecord.permission

import net.md_5.bungee.api.connection.ProxiedPlayer

class BungeeCordPermissionChecker : app.simplecloud.core.shared.permission.PermissionChecker<ProxiedPlayer> {

    override fun checkPermission(player: ProxiedPlayer, name: String): Boolean = player.hasPermission(name)
}
