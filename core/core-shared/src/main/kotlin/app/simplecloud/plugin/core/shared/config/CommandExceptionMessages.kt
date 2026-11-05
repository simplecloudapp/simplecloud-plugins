package app.simplecloud.plugin.core.shared.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class CommandExceptionMessages(
    val syntax: String = "<prefix> <error>Use <command> instead.",
    val permission: String = "<prefix> <error>You don't have permission to use this command.",
    val invalidSender: String = "<prefix> <error>You can't use this command here.",
    val failed: String = "<prefix> <error>The command failed because <reason>.",
    val argument: ArgumentExceptionMessages = ArgumentExceptionMessages(),
)

@ConfigSerializable
data class ArgumentExceptionMessages(
    val invalid: String = "<prefix> <error>That argument is invalid: <reason>.",
    val invalidNumber: String = "<prefix> <error><value> is not a number.",
    val numberTooSmall: String = "<prefix> <error>The number must be at least <min>.",
    val numberTooLarge: String = "<prefix> <error>The number must be at most <max>.",
    val invalidBoolean: String = "<prefix> <error>Use true or false.",
    val invalidEnum: String = "<prefix> <error><value> is not an option. Use one of <options>.",
    val invalidUuid: String = "<prefix> <error><value> is not a valid UUID.",
)
