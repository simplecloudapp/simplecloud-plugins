package app.simplecloud.plugin.notify.bungeecord.platform

import app.simplecloud.plugin.notify.bungeecord.command.BungeeCloudSender
import app.simplecloud.plugin.notify.shared.command.CloudSender
import app.simplecloud.plugin.notify.shared.platform.NotifyPlatform
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.md_5.bungee.api.plugin.Plugin
import java.io.File

class BungeeCordPlatformImpl(
    private val plugin: Plugin,
    private val adventure: BungeeAudiences
) : NotifyPlatform {

    override fun getDataDirectory(): File {
        return plugin.dataFolder
    }

    override fun getOnlinePlayers(): Collection<CloudSender> {
        return plugin.proxy.players.map { player -> BungeeCloudSender(player, adventure) }
    }
}
