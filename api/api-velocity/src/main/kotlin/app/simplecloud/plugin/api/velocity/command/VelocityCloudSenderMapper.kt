package app.simplecloud.plugin.api.velocity.command

import com.velocitypowered.api.command.CommandSource
import org.incendo.cloud.SenderMapper

object VelocityCloudSenderMapper : SenderMapper<CommandSource, VelocityCloudSender> {

    override fun map(base: CommandSource): VelocityCloudSender {
        return VelocityCloudSender(base)
    }

    override fun reverse(mapped: VelocityCloudSender): CommandSource {
        return mapped.getCommandSource()
    }

}
