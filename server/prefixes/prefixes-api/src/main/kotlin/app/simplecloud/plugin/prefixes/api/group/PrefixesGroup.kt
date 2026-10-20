package app.simplecloud.plugin.prefixes.api.group

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.UUID
import java.util.concurrent.CompletableFuture

interface PrefixesGroup {

    val name: String

    val priority: Int

    val permission: String

    val prefix: Component

    val suffix: Component

    val color: TextColor

    val displayName: String

    val chatFormat: String

    fun containsPlayer(id: UUID): CompletableFuture<Boolean>
}
