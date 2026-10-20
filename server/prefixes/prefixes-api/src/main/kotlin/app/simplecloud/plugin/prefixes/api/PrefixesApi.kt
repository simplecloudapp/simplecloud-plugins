package app.simplecloud.plugin.prefixes.api

import app.simplecloud.plugin.prefixes.api.group.GroupProvider
import app.simplecloud.plugin.prefixes.api.group.PrefixesGroup
import app.simplecloud.plugin.prefixes.api.group.PrefixesPlayerData
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.UUID
import java.util.concurrent.CompletableFuture

interface PrefixesApi {

    fun getGroups(): CompletableFuture<Collection<PrefixesGroup>>

    fun getPrimaryGroup(id: UUID): CompletableFuture<PrefixesGroup?>

    fun getPrefixData(id: UUID): CompletableFuture<PrefixesPlayerData>

    fun getPrefixData(id: UUID, viewer: Audience): CompletableFuture<PrefixesPlayerData>

    fun setPrefix(id: UUID, prefix: Component)

    fun setPrefix(id: UUID, prefix: Component, viewers: Audience)

    fun setSuffix(id: UUID, suffix: Component)

    fun setSuffix(id: UUID, suffix: Component, viewers: Audience)

    fun setColor(id: UUID, color: TextColor)

    fun setColor(id: UUID, color: TextColor, viewers: Audience)

    fun setPriority(id: UUID, priority: Int)

    fun setPriority(id: UUID, priority: Int, viewers: Audience)

    fun setGroup(id: UUID, group: String): CompletableFuture<Boolean>

    fun setGroup(id: UUID, group: String, viewers: Audience): CompletableFuture<Boolean>

    fun setGroup(targets: Audience, group: String): CompletableFuture<Boolean>

    fun setGroup(targets: Audience, group: String, viewers: Audience): CompletableFuture<Boolean>

    fun setGroup(id: UUID, group: PrefixesGroup)

    fun setGroup(id: UUID, group: PrefixesGroup, viewers: Audience)

    fun setGroup(targets: Audience, group: PrefixesGroup)

    fun setGroup(targets: Audience, group: PrefixesGroup, viewers: Audience)

    fun reset(id: UUID)

    fun reset(targets: Audience)

    fun reset(targets: Audience, viewers: Audience)

    fun addGroup(group: PrefixesGroup): CompletableFuture<Boolean>

    fun registerGroupProvider(provider: GroupProvider)

    fun unregisterGroupProvider(name: String)

    fun getGroupProvider(): GroupProvider

    fun getGroupProviders(): Collection<GroupProvider>

    fun refreshPlayer(id: UUID)

    fun refreshAll()
}
