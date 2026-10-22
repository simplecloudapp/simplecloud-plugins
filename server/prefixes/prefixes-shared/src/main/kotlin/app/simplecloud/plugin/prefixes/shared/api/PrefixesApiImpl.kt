package app.simplecloud.plugin.prefixes.shared.api

import app.simplecloud.plugin.prefixes.api.PrefixesApi
import app.simplecloud.plugin.prefixes.api.group.GroupProvider
import app.simplecloud.plugin.prefixes.api.group.PrefixesGroup
import app.simplecloud.plugin.prefixes.api.group.PrefixesPlayerData
import app.simplecloud.plugin.prefixes.shared.PrefixesConstants
import app.simplecloud.plugin.prefixes.shared.data.PrefixesDataRegistry
import app.simplecloud.plugin.prefixes.shared.data.PrefixesOverride
import app.simplecloud.plugin.prefixes.shared.group.GroupProviderRegistry
import app.simplecloud.plugin.prefixes.shared.platform.PrefixesListener
import app.simplecloud.plugin.prefixes.shared.platform.PrefixesPlatform
import app.simplecloud.plugin.prefixes.shared.utilities.AudienceResolver
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.UUID
import java.util.concurrent.CompletableFuture

class PrefixesApiImpl(
    private val registry: GroupProviderRegistry,
    private val dataRegistry: PrefixesDataRegistry,
    private val listeners: List<PrefixesListener>,
    private val platform: PrefixesPlatform,
) : PrefixesApi {

    override fun getGroups(): CompletableFuture<Collection<PrefixesGroup>> = registry.getCurrentGroupProvider().getGroups()

    override fun getPrimaryGroup(player: UUID): CompletableFuture<PrefixesGroup?> = registry.getCurrentGroupProvider().getGroup(player)

    override fun getPrefixData(player: UUID): CompletableFuture<PrefixesPlayerData> = PrefixesConstants.SCOPE.future { dataRegistry.getData(player, getPrimaryGroup(player).await()) }

    override fun getPrefixData(player: UUID, viewer: Audience): CompletableFuture<PrefixesPlayerData> {
        val viewerId = AudienceResolver.resolveId(viewer) ?: return getPrefixData(player)
        return PrefixesConstants.SCOPE.future { dataRegistry.getData(player, getPrimaryGroup(player).await(), viewerId) }
    }

    override fun setPrefix(player: UUID, prefix: Component) = set(player, PrefixesOverride(prefix = prefix))

    override fun setPrefix(player: UUID, prefix: Component, viewers: Audience) = set(player, PrefixesOverride(prefix = prefix), viewers)

    override fun setSuffix(player: UUID, suffix: Component) = set(player, PrefixesOverride(suffix = suffix))

    override fun setSuffix(player: UUID, suffix: Component, viewers: Audience) = set(player, PrefixesOverride(suffix = suffix), viewers)

    override fun setColor(player: UUID, color: TextColor) = set(player, PrefixesOverride(color = color))

    override fun setColor(player: UUID, color: TextColor, viewers: Audience) = set(player, PrefixesOverride(color = color), viewers)

    override fun setPriority(player: UUID, priority: Int) = set(player, PrefixesOverride(priority = priority))

    override fun setPriority(player: UUID, priority: Int, viewers: Audience) = set(player, PrefixesOverride(priority = priority), viewers)

    override fun setGroup(player: UUID, group: String): CompletableFuture<Boolean> {
        return PrefixesConstants.SCOPE.future {
            val found = findGroup(group) ?: return@future false
            setGroup(player, found)
            true
        }
    }

    override fun setGroup(player: UUID, group: String, viewers: Audience): CompletableFuture<Boolean> {
        return PrefixesConstants.SCOPE.future {
            val found = findGroup(group) ?: return@future false
            setGroup(player, found, viewers)
            true
        }
    }

    override fun setGroup(targets: Audience, group: String): CompletableFuture<Boolean> {
        return PrefixesConstants.SCOPE.future {
            val found = findGroup(group) ?: return@future false
            setGroup(targets, found)
            true
        }
    }

    override fun setGroup(targets: Audience, group: String, viewers: Audience): CompletableFuture<Boolean> {
        return PrefixesConstants.SCOPE.future {
            val found = findGroup(group) ?: return@future false
            setGroup(targets, found, viewers)
            true
        }
    }

    override fun setGroup(player: UUID, group: PrefixesGroup) = set(player, PrefixesOverride.of(group))

    override fun setGroup(player: UUID, group: PrefixesGroup, viewers: Audience) = set(player, PrefixesOverride.of(group), viewers)

    override fun setGroup(targets: Audience, group: PrefixesGroup) {
        AudienceResolver.resolveIds(targets).forEach { player -> setGroup(player, group) }
    }

    override fun setGroup(targets: Audience, group: PrefixesGroup, viewers: Audience) {
        val viewerIds = AudienceResolver.resolveIds(viewers)
        AudienceResolver.resolveIds(targets).forEach { player -> set(player, PrefixesOverride.of(group), viewerIds) }
    }

    override fun reset(player: UUID) {
        dataRegistry.reset(player)
        notifyDataChange(player)
    }

    override fun reset(targets: Audience) {
        AudienceResolver.resolveIds(targets).forEach(::reset)
    }

    override fun reset(targets: Audience, viewers: Audience) {
        val viewerIds = AudienceResolver.resolveIds(viewers)
        AudienceResolver.resolveIds(targets).forEach { player ->
            viewerIds.forEach { viewer -> dataRegistry.reset(player, viewer) }
            notifyDataChange(player)
        }
    }

    override fun setVanished(player: UUID, vanished: Boolean) {
        if (!platform.isOnline(player)) return
        dataRegistry.setVanished(player, vanished)
        notifyDataChange(player)
    }

    override fun isVanished(player: UUID): Boolean = dataRegistry.isVanished(player) || platform.isVanished(player)

    override fun addGroup(group: PrefixesGroup): CompletableFuture<Boolean> = registry.getCurrentGroupProvider().addGroup(group)

    override fun registerGroupProvider(provider: GroupProvider) = registry.register(provider)

    override fun unregisterGroupProvider(name: String) = registry.unregister(name)

    override fun getGroupProvider(): GroupProvider = registry.getCurrentGroupProvider()

    override fun getGroupProviders(): Collection<GroupProvider> = registry.getAllGroupProviders()

    override fun refreshPlayer(player: UUID) {
        listeners.forEach { it.onPlayerUpdate(player) }
    }

    override fun refreshAll() {
        listeners.forEach(PrefixesListener::onAllPlayersUpdate)
    }

    private fun set(player: UUID, override: PrefixesOverride) {
        if (!platform.isOnline(player)) return
        dataRegistry.set(player, override)
        notifyDataChange(player)
    }

    private fun set(player: UUID, override: PrefixesOverride, viewers: Audience) {
        set(player, override, AudienceResolver.resolveIds(viewers))
    }

    private fun set(player: UUID, override: PrefixesOverride, viewers: List<UUID>) {
        if (!platform.isOnline(player)) return
        viewers.forEach { viewer -> dataRegistry.set(player, viewer, override) }
        notifyDataChange(player)
    }

    private suspend fun findGroup(name: String): PrefixesGroup? = getGroups().await().firstOrNull { group -> group.name.equals(name, ignoreCase = true) }

    private fun notifyDataChange(player: UUID) {
        listeners.forEach { listener -> listener.onPlayerDataChange(player) }
    }
}
