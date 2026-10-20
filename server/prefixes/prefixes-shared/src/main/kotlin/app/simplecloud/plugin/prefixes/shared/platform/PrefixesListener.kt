package app.simplecloud.plugin.prefixes.shared.platform

import java.util.UUID

interface PrefixesListener {

    fun onReload()

    fun onShutdown()

    fun onPlayerUpdate(id: UUID)

    fun onPlayerDataChange(id: UUID)

    fun onAllPlayersUpdate()
}
