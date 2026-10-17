package app.simplecloud.plugin.notify.shared.notification

import app.simplecloud.plugin.notify.shared.command.CloudSender
import net.kyori.adventure.text.Component

class Notification(
    val message: Component,
    val type: NotificationType
) {

    fun sendTo(sender: CloudSender) {
        if (type.canReceive(sender)) {
            sender.sendMessage(message)
        }
    }
}
