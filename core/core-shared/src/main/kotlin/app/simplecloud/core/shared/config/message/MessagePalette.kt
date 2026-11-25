package app.simplecloud.core.shared.config.message

import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.slf4j.LoggerFactory

object MessagePalette {

    const val PREFIX = "<brand><b>⚡</b></brand>"

    val defaults: Map<String, String> = mapOf(
        "brand" to "#3B95FF",
        "interactive" to "#62ADFF",
        "highlight" to "#CFE4FF",
        "muted" to "#A1A7B5",
        "separator" to "#5F5F63",
        "info" to "#4FA3FF",
        "success" to "#6AD13C",
        "warning" to "#FF9029",
        "error" to "#FF3B30",
        "mint" to "#1FD98E",
        "violet" to "#9566FF",
        "pink" to "#E94BA6",
        "gold" to "#F2C230",
        "cyan" to "#22C3DB",
        "amber" to "#FFA329",
    )

    private val logger = LoggerFactory.getLogger(MessagePalette::class.java)

    fun resolver(palette: Map<String, String>): TagResolver {
        val validPalette = palette.filter { (name, hex) -> isValid(name, hex) }
        return TagResolver.resolver(
            (defaults + validPalette).mapNotNull { (name, hex) ->
                val color = TextColor.fromHexString(hex) ?: return@mapNotNull null
                TagResolver.resolver(name, Tag.styling(color))
            },
        )
    }

    private fun isValid(name: String, hex: String): Boolean {
        if (TextColor.fromHexString(hex) != null) return true

        logger.warn("Ignoring the invalid palette color '{}' of '{}'", hex, name)
        return false
    }
}
