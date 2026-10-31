package app.simplecloud.plugin.core.shared.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class CommandExceptionMessages(
    val syntax: String = "<prefix> <#DC2626>Use <#F8FAFC><command> <#DC2626>instead.",
    val permission: String = "<prefix> <#DC2626>You do not have permission to use this command.",
    val invalidSender: String = "<prefix> <#DC2626>You cannot use this command.",
    val failed: String = "<prefix> <#DC2626>The command failed because <#F8FAFC><reason><#DC2626>.",
    val argument: ArgumentExceptionMessages = ArgumentExceptionMessages(),
)

@ConfigSerializable
data class ArgumentExceptionMessages(
    val invalid: String = "<prefix> <#DC2626>Invalid argument: <#F8FAFC><reason><#DC2626>.",
    val invalidNumber: String = "<prefix> <#DC2626><#F8FAFC><value> <#DC2626>is not a valid number.",
    val numberTooSmall: String = "<prefix> <#DC2626>The number must be at least <#F8FAFC><min><#DC2626>.",
    val numberTooLarge: String = "<prefix> <#DC2626>The number must be at most <#F8FAFC><max><#DC2626>.",
    val invalidBoolean: String = "<prefix> <#DC2626>Use <#F8FAFC>true <#DC2626>or <#F8FAFC>false<#DC2626>.",
    val invalidEnum: String = "<prefix> <#DC2626><#F8FAFC><value> <#DC2626>is invalid. Use one of <#F8FAFC><options><#DC2626>.",
    val invalidUuid: String = "<prefix> <#DC2626><#F8FAFC><value> <#DC2626>is not a valid UUID.",
)
