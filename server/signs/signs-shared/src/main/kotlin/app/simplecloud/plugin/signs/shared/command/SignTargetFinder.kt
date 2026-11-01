package app.simplecloud.plugin.signs.shared.command

import app.simplecloud.plugin.signs.shared.config.location.SignLocation

interface SignTargetFinder<C> {

    suspend fun find(sender: C): SignLocation?
}
