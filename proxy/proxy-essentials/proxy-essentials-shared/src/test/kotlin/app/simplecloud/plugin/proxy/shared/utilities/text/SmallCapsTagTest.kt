package app.simplecloud.plugin.proxy.shared.utilities.text

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import kotlin.test.Test
import kotlin.test.assertEquals

class SmallCapsTagTest {

    private val miniMessage = MiniMessage.miniMessage()

    @Test
    fun `converts letters to small caps and keeps everything else`() {
        assertEquals("ᴍʏ ɴᴇᴛᴡᴏʀᴋ 2.0!", render("<smallcaps>My Network 2.0!</smallcaps>"))
    }

    @Test
    fun `converts nested and inserted text`() {
        val name = Placeholder.unparsed("name", "SimpleCloud")
        assertEquals("ꜱɪᴍᴘʟᴇᴄʟᴏᴜᴅ ʀᴜɴꜱ", render("<smallcaps><bold><name></bold> runs</smallcaps>", name))
    }

    private fun render(text: String, vararg extra: TagResolver): String = PlainTextComponentSerializer.plainText()
        .serialize(miniMessage.deserialize(text, SmallCapsTag.resolver, *extra))
}
