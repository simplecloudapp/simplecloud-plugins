package app.simplecloud.plugin.core.velocity.command

import com.velocitypowered.api.command.CommandSource
import org.incendo.cloud.SenderMapper

object VelocityCloudSenderMapper : SenderMapper<CommandSource, VelocityCloudSender> {

    override fun map(base: CommandSource): VelocityCloudSender = VelocityCloudSender(base)

    override fun reverse(mapped: VelocityCloudSender): CommandSource = mapped.getCommandSource()
}
