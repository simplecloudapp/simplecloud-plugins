package app.simplecloud.plugin.notify.bungeecord.platform

import app.simplecloud.plugin.core.bungeecord.command.BungeeCloudSender
import app.simplecloud.plugin.core.shared.command.CloudSender
import app.simplecloud.plugin.notify.shared.platform.NotifyPlatform
import net.md_5.bungee.api.plugin.Plugin
import java.io.File

class BungeeCordPlatformImpl(
    private val plugin: Plugin,
) : NotifyPlatform {

    override fun getDataDirectory(): File = plugin.dataFolder

    override fun getOnlinePlayers(): Collection<CloudSender> = plugin.proxy.players.map(::BungeeCloudSender)
}
