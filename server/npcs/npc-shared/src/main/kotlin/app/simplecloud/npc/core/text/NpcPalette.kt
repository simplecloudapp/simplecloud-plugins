package app.simplecloud.npc.core.text

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/** The color tags of the shared SimpleCloud palette, like <brand> and <error>. The colors come from messages.yml. */
object NpcPalette : TagResolver {

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

    @Volatile
    private var colors: Map<String, TextColor> = parse(defaults)

    val miniMessage: MiniMessage = MiniMessage.builder()
        .tags(TagResolver.resolver(TagResolver.standard(), this))
        .build()

    fun update(palette: Map<String, String>) {
        colors = parse(defaults + palette)
    }

    override fun resolve(name: String, arguments: ArgumentQueue, ctx: Context): Tag? {
        val color = colors[name] ?: return null
        return Tag.styling(color)
    }

    override fun has(name: String): Boolean = name in colors

    fun color(name: String): TextColor = colors[name] ?: NamedTextColor.WHITE

    private fun parse(palette: Map<String, String>): Map<String, TextColor> = palette.mapNotNull { (name, hex) ->
        val color = TextColor.fromHexString(hex) ?: return@mapNotNull null
        name to color
    }.toMap()
}
