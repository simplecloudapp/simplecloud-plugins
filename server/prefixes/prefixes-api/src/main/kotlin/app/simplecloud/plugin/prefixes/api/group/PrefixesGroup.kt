package app.simplecloud.plugin.prefixes.api.group

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.UUID
import java.util.concurrent.CompletableFuture

public interface PrefixesGroup {

    public val name: String

    public val priority: Int

    public val permission: String

    public val prefix: Component

    public val suffix: Component

    public val color: TextColor

    public val displayName: String

    public val chatFormat: String

    public fun containsPlayer(id: UUID): CompletableFuture<Boolean>
}
