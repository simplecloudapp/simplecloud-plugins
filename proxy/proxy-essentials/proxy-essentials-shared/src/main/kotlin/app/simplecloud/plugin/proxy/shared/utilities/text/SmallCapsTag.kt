package app.simplecloud.plugin.proxy.shared.utilities.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.tag.Modifying
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

object SmallCapsTag {

    private const val LETTERS = "abcdefghijklmnopqrstuvwxyz"
    private const val SMALL_CAPS = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ"

    val resolver: TagResolver = TagResolver.resolver("smallcaps") { _, _ ->
        Modifying { current, _ ->
            val component = current.children(emptyList())
            if (component !is TextComponent) return@Modifying component
            component.content(convert(component.content()))
        }
    }

    private fun convert(text: String): String = text.map { char ->
        val index = LETTERS.indexOf(char.lowercaseChar())
        if (index == -1) char else SMALL_CAPS[index]
    }.joinToString("")
}
