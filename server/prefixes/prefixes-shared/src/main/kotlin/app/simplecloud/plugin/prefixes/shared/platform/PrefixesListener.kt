package app.simplecloud.plugin.prefixes.shared.platform

import net.kyori.adventure.text.Component
import java.util.UUID

interface PrefixesListener {

    fun onReload()

    fun onShutdown()

    fun onPlayerUpdate(id: UUID)

    fun onPlayerDataChange(id: UUID)

    fun onAllPlayersUpdate()

    fun onChatMessage(message: Component)

    fun onTablistRequest()
}
