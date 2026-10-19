package app.simplecloud.plugin.core.paper.command

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.SenderMapper

object PaperCloudSenderMapper : SenderMapper<CommandSourceStack, PaperCloudSender> {

    override fun map(base: CommandSourceStack): PaperCloudSender = PaperCloudSender(base)

    override fun reverse(mapped: PaperCloudSender): CommandSourceStack = mapped.getSourceStack()
}
