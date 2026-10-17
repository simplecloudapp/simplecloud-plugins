package app.simplecloud.plugin.api.bungeecord.command

import net.md_5.bungee.api.CommandSender
import org.incendo.cloud.SenderMapper

class BungeeCloudSenderMapper : SenderMapper<CommandSender, BungeeCloudSender> {

    override fun map(base: CommandSender): BungeeCloudSender {
        return BungeeCloudSender(base)
    }

    override fun reverse(mapped: BungeeCloudSender): CommandSender {
        return mapped.getCommandSender()
    }

}
