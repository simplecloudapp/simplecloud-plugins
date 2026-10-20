package app.simplecloud.plugin.prefixes.minestom

import net.luckperms.api.LuckPerms
import net.minestom.server.entity.Player
import java.nio.file.Path
import java.util.function.BiPredicate

@ConsistentCopyVisibility
data class PrefixesMinestomBuilder internal constructor(
    private val directory: Path,
    private val permission: BiPredicate<Player, String>? = null,
    private val registerCommands: Boolean = true,
    private val luckPerms: LuckPerms? = null,
) {

    fun permission(check: BiPredicate<Player, String>): PrefixesMinestomBuilder = copy(permission = check)

    fun registerCommands(enabled: Boolean): PrefixesMinestomBuilder = copy(registerCommands = enabled)

    fun luckPerms(luckPerms: LuckPerms): PrefixesMinestomBuilder = copy(luckPerms = luckPerms)

    fun enable(): PrefixesMinestom = PrefixesMinestom.start(directory, permission, registerCommands, luckPerms)
}
