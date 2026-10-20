package app.simplecloud.plugin.prefixes.minestom.platform

import app.simplecloud.plugin.core.minestom.permission.MinestomPermissions
import app.simplecloud.plugin.core.shared.permission.PermissionChecker
import app.simplecloud.plugin.prefixes.shared.platform.PrefixesPlatform
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.minestom.server.MinecraftServer
import java.io.File
import java.nio.file.Path
import java.util.UUID

class MinestomPlatformImpl(
    private val path: Path,
    private val permissions: MinestomPermissions,
    private val luckPerms: LuckPerms?,
) : PrefixesPlatform {

    override fun getDataDirectory(): File = path.toFile()

    override fun getPermissionChecker(): PermissionChecker<UUID> = PermissionChecker { id, permission ->
        val player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(id)
        permissions.hasCheck && player != null && permissions.hasPermission(player, permission)
    }

    override fun getLuckPerms(): LuckPerms? = luckPerms ?: runCatching { LuckPermsProvider.get() }.getOrNull()

    override fun getPlayerName(id: UUID): String = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(id)?.username ?: id.toString()

    override fun isOnline(id: UUID): Boolean = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(id) != null
}
