package app.simplecloud.plugin.prefixes.api

import app.simplecloud.plugin.prefixes.api.group.GroupProvider
import app.simplecloud.plugin.prefixes.api.group.PrefixesGroup
import app.simplecloud.plugin.prefixes.api.group.PrefixesPlayerData
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Entry point for interacting with prefixes.
 */
public interface PrefixesApi {

    /** Returns all groups of the active [GroupProvider], ordered by priority from highest to lowest. */
    public fun getGroups(): CompletableFuture<Collection<PrefixesGroup>>

    /** Returns the primary [PrefixesGroup] of [player] from the active [GroupProvider], or null if none matches. */
    public fun getPrimaryGroup(player: UUID): CompletableFuture<PrefixesGroup?>

    /** Returns the [PrefixesPlayerData] of [player] as seen by all players. */
    public fun getPrefixData(player: UUID): CompletableFuture<PrefixesPlayerData>

    /** Returns the [PrefixesPlayerData] of [player] as seen by [viewer], including overrides set only for [viewer]. */
    public fun getPrefixData(player: UUID, viewer: Audience): CompletableFuture<PrefixesPlayerData>

    /** Sets the [prefix] of [player] for all players. */
    public fun setPrefix(player: UUID, prefix: Component)

    /** Sets the [prefix] of [player] only for [viewers]. */
    public fun setPrefix(player: UUID, prefix: Component, viewers: Audience)

    /** Sets the [suffix] of [player] for all players. */
    public fun setSuffix(player: UUID, suffix: Component)

    /** Sets the [suffix] of [player] only for [viewers]. */
    public fun setSuffix(player: UUID, suffix: Component, viewers: Audience)

    /** Sets the [color] of [player] for all players. */
    public fun setColor(player: UUID, color: TextColor)

    /** Sets the [color] of [player] only for [viewers]. */
    public fun setColor(player: UUID, color: TextColor, viewers: Audience)

    /** Sets the tablist [priority] of [player] for all players. */
    public fun setPriority(player: UUID, priority: Int)

    /** Sets the tablist [priority] of [player] only for [viewers]. */
    public fun setPriority(player: UUID, priority: Int, viewers: Audience)

    /** Sets the [group] of [player] for all players. Completes with false if the group doesn't exist. */
    public fun setGroup(player: UUID, group: String): CompletableFuture<Boolean>

    /** Sets the [group] of [player] only for [viewers]. Completes with false if the group doesn't exist. */
    public fun setGroup(player: UUID, group: String, viewers: Audience): CompletableFuture<Boolean>

    /** Sets the [group] of [targets] for all players. Completes with false if the group doesn't exist. */
    public fun setGroup(targets: Audience, group: String): CompletableFuture<Boolean>

    /** Sets the [group] of [targets] only for [viewers]. Completes with false if the group doesn't exist. */
    public fun setGroup(targets: Audience, group: String, viewers: Audience): CompletableFuture<Boolean>

    /** Sets the [group] of [player] for all players. */
    public fun setGroup(player: UUID, group: PrefixesGroup)

    /** Sets the [group] of [player] only for [viewers]. */
    public fun setGroup(player: UUID, group: PrefixesGroup, viewers: Audience)

    /** Sets the [group] of [targets] for all players. */
    public fun setGroup(targets: Audience, group: PrefixesGroup)

    /** Sets the [group] of [targets] only for [viewers]. */
    public fun setGroup(targets: Audience, group: PrefixesGroup, viewers: Audience)

    /** Removes all overrides of [player], including those set only for specific viewers. */
    public fun reset(player: UUID)

    /** Removes all overrides of [targets], including those set only for specific viewers. */
    public fun reset(targets: Audience)

    /** Removes the overrides of [targets] that were set only for [viewers]. */
    public fun reset(targets: Audience, viewers: Audience)

    /** Sets whether [player] is [vanished] and therefore hidden from the tablist. */
    public fun setVanished(player: UUID, vanished: Boolean)

    /** Checks whether [player] is vanished, either through [setVanished] or the platform. */
    public fun isVanished(player: UUID): Boolean

    /** Adds [group] to the active [GroupProvider]. Completes with false if the group could not be added. */
    public fun addGroup(group: PrefixesGroup): CompletableFuture<Boolean>

    /** Registers [provider], replacing any provider with the same name. */
    public fun registerGroupProvider(provider: GroupProvider)

    /** Unregisters the [GroupProvider] with the given [name]. */
    public fun unregisterGroupProvider(name: String)

    /** Returns the active [GroupProvider] selected by the configured source, or the config provider as fallback. */
    public fun getGroupProvider(): GroupProvider

    /** Returns all registered group providers. */
    public fun getGroupProviders(): Collection<GroupProvider>

    /** Refreshes the displayed prefixes of [player]. */
    public fun refreshPlayer(player: UUID)

    /** Refreshes the displayed prefixes of all online players. */
    public fun refreshAll()
}
