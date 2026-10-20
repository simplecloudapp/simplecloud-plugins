package app.simplecloud.plugin.prefixes.paper.platform

import app.simplecloud.plugin.core.shared.permission.PermissionChecker
import app.simplecloud.plugin.prefixes.shared.platform.PrefixesPlatform
import net.luckperms.api.LuckPerms
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.UUID

class PaperPlatformImpl(private val plugin: Plugin) : PrefixesPlatform {

    override fun getDataDirectory(): File = plugin.dataFolder

    override fun getPermissionChecker(): PermissionChecker<UUID> = PermissionChecker { id, permission ->
        Bukkit.getPlayer(id)?.hasPermission(permission) ?: false
    }

    override fun getLuckPerms(): LuckPerms? {
        if (!Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) return null
        return Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider
    }

    override fun getPlayerName(id: UUID): String = Bukkit.getOfflinePlayer(id).name ?: id.toString()

    override fun isOnline(id: UUID): Boolean = Bukkit.getPlayer(id) != null
}
