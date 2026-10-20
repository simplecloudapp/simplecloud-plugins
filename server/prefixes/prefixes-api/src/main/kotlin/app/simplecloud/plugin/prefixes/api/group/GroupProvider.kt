package app.simplecloud.plugin.prefixes.api.group

import java.util.UUID
import java.util.concurrent.CompletableFuture

interface GroupProvider {

    fun getName(): String

    fun getGroups(): CompletableFuture<Collection<PrefixesGroup>>

    fun getGroup(id: UUID): CompletableFuture<PrefixesGroup?>

    fun addGroup(group: PrefixesGroup): CompletableFuture<Boolean>
}
