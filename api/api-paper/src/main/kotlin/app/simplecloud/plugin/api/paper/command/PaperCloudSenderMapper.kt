package app.simplecloud.plugin.api.paper.command

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.SenderMapper

object PaperCloudSenderMapper : SenderMapper<CommandSourceStack, PaperCloudSender> {

    override fun map(base: CommandSourceStack): PaperCloudSender {
        return PaperCloudSender(base)
    }

    override fun reverse(mapped: PaperCloudSender): CommandSourceStack {
        return mapped.getSourceStack()
    }

}
