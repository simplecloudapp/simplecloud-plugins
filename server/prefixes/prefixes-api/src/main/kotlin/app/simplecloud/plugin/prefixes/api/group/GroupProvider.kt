package app.simplecloud.plugin.prefixes.api.group

import java.util.UUID
import java.util.concurrent.CompletableFuture

public interface GroupProvider {

    public fun getName(): String

    public fun getGroups(): CompletableFuture<Collection<PrefixesGroup>>

    public fun getGroup(id: UUID): CompletableFuture<PrefixesGroup?>

    public fun addGroup(group: PrefixesGroup): CompletableFuture<Boolean>
}
