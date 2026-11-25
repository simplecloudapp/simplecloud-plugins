package app.simplecloud.plugin.notify.shared.listener

import app.simplecloud.plugin.core.shared.command.CloudSender
import app.simplecloud.plugin.notify.shared.platform.NotifyPlatform
import java.io.File

class TestNotifyPlatform(
    private val senders: List<CloudSender>,
) : NotifyPlatform {

    override fun getDataDirectory(): File = error("The data directory is not used in tests")

    override fun getOnlinePlayers(): Collection<CloudSender> = senders
}
