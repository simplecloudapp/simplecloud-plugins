package app.simplecloud.plugin.notify.shared.listener

import app.simplecloud.plugin.core.shared.command.CloudSender
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

class TestNotifySender(
    private val uniqueId: UUID?,
    private val permissions: Set<String>,
) : CloudSender {

    private val messages = CopyOnWriteArrayList<Component>()

    fun plainMessages(): List<String> =
        messages.map { message -> PlainTextComponentSerializer.plainText().serialize(message) }

    override fun getUniqueId(): UUID? = uniqueId

    override fun hasPermission(permission: String): Boolean = permission in permissions

    override fun sendMessage(message: Component) {
        messages.add(message)
    }
}
