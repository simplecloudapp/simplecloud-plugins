package app.simplecloud.plugin.core.minestom.command

import app.simplecloud.plugin.core.minestom.permission.MinestomPermissions
import net.minestom.server.command.CommandSender
import org.incendo.cloud.SenderMapper

class MinestomCloudSenderMapper(private val permissions: MinestomPermissions) :
    SenderMapper<CommandSender, MinestomCloudSender> {

    override fun map(base: CommandSender): MinestomCloudSender = MinestomCloudSender(base, permissions)

    override fun reverse(mapped: MinestomCloudSender): CommandSender = mapped.getCommandSender()
}
