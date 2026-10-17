package app.simplecloud.plugin.api.minestom.permission

import net.minestom.server.command.CommandSender
import net.minestom.server.command.ConsoleSender
import net.minestom.server.entity.Player
import java.util.function.BiPredicate

/**
 * Utility for checking permissions on a Minestom server.
 * Without a [check], only players with permission level 4 have permissions.
 */
class MinestomPermissions(private val check: BiPredicate<Player, String>?) {

    val hasCheck: Boolean = check != null

    fun hasPermission(sender: CommandSender, permission: String): Boolean {
        if (permission.isEmpty()) return true
        if (sender is ConsoleSender) return true
        if (sender !is Player) return false

        if (check != null) return check.test(sender, permission)
        return sender.permissionLevel >= 4
    }

    fun getChecker(): MinestomPermissionChecker {
        return MinestomPermissionChecker(this)
    }

}
