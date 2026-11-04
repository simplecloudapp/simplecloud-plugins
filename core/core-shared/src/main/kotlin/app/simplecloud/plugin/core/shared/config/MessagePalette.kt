package app.simplecloud.plugin.core.shared.config

import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

object MessagePalette {

    val defaults: Map<String, String> = mapOf(
        "brand" to "#3B95FF",
        "interactive" to "#62ADFF",
        "highlight" to "#CFE4FF",
        "muted" to "#A8A8AB",
        "separator" to "#5F5F63",
        "info" to "#4FA3FF",
        "success" to "#6AD13C",
        "warning" to "#FF9029",
        "error" to "#F5464F",
        "mint" to "#1FD98E",
        "violet" to "#9566FF",
        "pink" to "#E94BA6",
        "gold" to "#F2C230",
        "cyan" to "#22C3DB",
        "amber" to "#FFA329",
    )

    fun resolver(palette: Map<String, String>): TagResolver = TagResolver.resolver(
        (defaults + palette).map { (name, hex) ->
            val color = requireNotNull(TextColor.fromHexString(hex)) { "Invalid palette color $name: $hex" }
            TagResolver.resolver(name, Tag.styling(color))
        },
    )
}
