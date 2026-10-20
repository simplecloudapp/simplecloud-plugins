package app.simplecloud.plugin.prefixes.api

import app.simplecloud.plugin.prefixes.api.group.GroupProvider
import app.simplecloud.plugin.prefixes.api.group.PrefixesGroup
import app.simplecloud.plugin.prefixes.api.group.PrefixesPlayerData
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.UUID
import java.util.concurrent.CompletableFuture

public interface PrefixesApi {

    public fun getGroups(): CompletableFuture<Collection<PrefixesGroup>>

    public fun getPrimaryGroup(id: UUID): CompletableFuture<PrefixesGroup?>

    public fun getPrefixData(id: UUID): CompletableFuture<PrefixesPlayerData>

    public fun getPrefixData(id: UUID, viewer: Audience): CompletableFuture<PrefixesPlayerData>

    public fun setPrefix(id: UUID, prefix: Component)

    public fun setPrefix(id: UUID, prefix: Component, viewers: Audience)

    public fun setSuffix(id: UUID, suffix: Component)

    public fun setSuffix(id: UUID, suffix: Component, viewers: Audience)

    public fun setColor(id: UUID, color: TextColor)

    public fun setColor(id: UUID, color: TextColor, viewers: Audience)

    public fun setPriority(id: UUID, priority: Int)

    public fun setPriority(id: UUID, priority: Int, viewers: Audience)

    public fun setGroup(id: UUID, group: String): CompletableFuture<Boolean>

    public fun setGroup(id: UUID, group: String, viewers: Audience): CompletableFuture<Boolean>

    public fun setGroup(targets: Audience, group: String): CompletableFuture<Boolean>

    public fun setGroup(targets: Audience, group: String, viewers: Audience): CompletableFuture<Boolean>

    public fun setGroup(id: UUID, group: PrefixesGroup)

    public fun setGroup(id: UUID, group: PrefixesGroup, viewers: Audience)

    public fun setGroup(targets: Audience, group: PrefixesGroup)

    public fun setGroup(targets: Audience, group: PrefixesGroup, viewers: Audience)

    public fun reset(id: UUID)

    public fun reset(targets: Audience)

    public fun reset(targets: Audience, viewers: Audience)

    public fun setVanished(id: UUID, vanished: Boolean)

    public fun isVanished(id: UUID): Boolean

    public fun addGroup(group: PrefixesGroup): CompletableFuture<Boolean>

    public fun registerGroupProvider(provider: GroupProvider)

    public fun unregisterGroupProvider(name: String)

    public fun getGroupProvider(): GroupProvider

    public fun getGroupProviders(): Collection<GroupProvider>

    public fun refreshPlayer(id: UUID)

    public fun refreshAll()
}
