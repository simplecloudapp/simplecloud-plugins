package app.simplecloud.plugin.signs.shared

import app.simplecloud.plugin.signs.shared.config.location.SignLocation

interface SignRenderer {

    fun render(location: SignLocation, sign: RenderedSign)

    fun clear(location: SignLocation)
}
