package app.simplecloud.plugin.command.shared.command

import org.incendo.cloud.CommandManager
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.internal.CommandRegistrationHandler

class TestCommandManager :
    CommandManager<TestCommandSender>(
        ExecutionCoordinator.simpleCoordinator(),
        CommandRegistrationHandler.nullCommandRegistrationHandler(),
    ) {

    override fun hasPermission(sender: TestCommandSender, permission: String): Boolean = sender.hasPermission(permission)
}
