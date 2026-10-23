package app.simplecloud.plugin.prefixes.shared.sync

import net.kyori.adventure.text.Component

interface SyncTarget {

    fun broadcast(message: Component)

    fun publishAll()
}
