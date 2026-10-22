package app.simplecloud.plugin.prefixes.api.group

import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Represents a source of prefixes groups.
 */
public interface GroupProvider {

    /** Returns the name of this provider, used to select it as group source. */
    public fun getName(): String

    /** Returns all groups of this provider, ordered by priority from highest to lowest. */
    public fun getGroups(): CompletableFuture<Collection<PrefixesGroup>>

    /** Returns the primary [PrefixesGroup] of [player], or null if none matches. */
    public fun getGroup(player: UUID): CompletableFuture<PrefixesGroup?>

    /** Adds [group] to this provider. Completes with false if a group with the same name already exists. */
    public fun addGroup(group: PrefixesGroup): CompletableFuture<Boolean>
}
