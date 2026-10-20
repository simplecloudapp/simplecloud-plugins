package app.simplecloud.plugin.prefixes.shared.group.config

import app.simplecloud.plugin.core.shared.adventure.miniMessage
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.core.shared.permission.PermissionChecker
import app.simplecloud.plugin.prefixes.api.group.GroupProvider
import app.simplecloud.plugin.prefixes.api.group.PrefixesGroup
import app.simplecloud.plugin.prefixes.shared.PrefixesConstants
import app.simplecloud.plugin.prefixes.shared.config.ConfigGroup
import app.simplecloud.plugin.prefixes.shared.config.PrefixesConfig
import app.simplecloud.plugin.prefixes.shared.utilities.ColorParser
import kotlinx.coroutines.future.future
import java.util.UUID
import java.util.concurrent.CompletableFuture

class ConfigGroupProvider(
    private val configFactory: ConfigurationFactory<PrefixesConfig>,
    private val permissionChecker: PermissionChecker<UUID>,
) : GroupProvider {

    private val logger = PrefixesConstants.LOGGER

    override fun getName(): String = PrefixesConstants.CONFIG_SOURCE

    override fun getGroups(): CompletableFuture<Collection<PrefixesGroup>> = CompletableFuture.completedFuture(loadGroups())

    override fun getGroup(id: UUID): CompletableFuture<PrefixesGroup?> {
        val groups = loadGroups()
        val group = groups.firstOrNull { !isDefault(it) && it.hasPermission(id) } ?: groups.firstOrNull(::isDefault)
        return CompletableFuture.completedFuture(group)
    }

    override fun addGroup(group: PrefixesGroup): CompletableFuture<Boolean> {
        return PrefixesConstants.SCOPE.future {
            synchronized(this@ConfigGroupProvider) {
                val config = configFactory.get()
                if (config.groups.any { it.name.equals(group.name, ignoreCase = true) }) {
                    return@synchronized false
                }

                try {
                    configFactory.save(config.copy(groups = config.groups + createConfigGroup(group)))
                    true
                } catch (e: Exception) {
                    logger.error("Failed to save group '${group.name}' to config", e)
                    false
                }
            }
        }
    }

    private fun loadGroups(): List<ConfigPrefixesGroup> = configFactory.get().groups
        .map { ConfigPrefixesGroup(it, permissionChecker) }
        .sortedByDescending { it.priority }

    private fun isDefault(group: PrefixesGroup): Boolean =
        group.name.equals(configFactory.get().general.defaultGroup, ignoreCase = true)

    private fun createConfigGroup(group: PrefixesGroup) = ConfigGroup(
        name = group.name,
        priority = group.priority,
        permission = group.permission,
        prefix = miniMessage.serialize(group.prefix),
        suffix = miniMessage.serialize(group.suffix),
        color = ColorParser.serialize(group.color),
        displayName = group.displayName,
        chatFormat = group.chatFormat,
    )
}
