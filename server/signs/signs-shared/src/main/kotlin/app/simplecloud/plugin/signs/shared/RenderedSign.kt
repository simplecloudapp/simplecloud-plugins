package app.simplecloud.plugin.signs.shared

import net.kyori.adventure.text.Component

data class RenderedSign(
    val lines: List<Component>,
    val behindBlock: String?,
)
