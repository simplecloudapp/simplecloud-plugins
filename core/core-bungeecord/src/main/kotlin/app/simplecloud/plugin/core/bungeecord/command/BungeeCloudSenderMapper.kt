package app.simplecloud.plugin.core.bungeecord.command

import net.md_5.bungee.api.CommandSender
import org.incendo.cloud.SenderMapper

class BungeeCloudSenderMapper : SenderMapper<CommandSender, BungeeCloudSender> {

    override fun map(base: CommandSender): BungeeCloudSender = BungeeCloudSender(base)

    override fun reverse(mapped: BungeeCloudSender): CommandSender = mapped.getCommandSender()
}
