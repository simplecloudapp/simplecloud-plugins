package app.simplecloud.plugin.command.shared.command

import app.simplecloud.core.shared.command.CloudSender
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

class TestCommandSender : CloudSender {

    private val messages = CopyOnWriteArrayList<Component>()

    fun plainMessages(): List<String> =
        messages.map { message -> PlainTextComponentSerializer.plainText().serialize(message) }

    fun clear() {
        messages.clear()
    }

    override fun getUniqueId(): UUID? = null

    override fun hasPermission(permission: String): Boolean = true

    override fun sendMessage(message: Component) {
        messages.add(message)
    }
}
