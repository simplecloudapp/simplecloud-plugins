package app.simplecloud.plugin.prefixes.shared.platform

import app.simplecloud.plugin.core.shared.permission.PermissionChecker
import net.luckperms.api.LuckPerms
import java.io.File
import java.util.UUID

interface PrefixesPlatform {

    fun getDataDirectory(): File

    fun getPermissionChecker(): PermissionChecker<UUID>

    fun getLuckPerms(): LuckPerms?

    fun getPlayerName(id: UUID): String

    fun isOnline(id: UUID): Boolean

    fun isVanished(id: UUID): Boolean
}
