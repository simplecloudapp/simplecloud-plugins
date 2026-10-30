package app.simplecloud.plugin.core.shared.command

import app.simplecloud.api.CloudApiException
import app.simplecloud.plugin.core.shared.adventure.tags
import app.simplecloud.plugin.core.shared.config.AbstractCommandMessageConfig
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.incendo.cloud.CommandManager
import org.incendo.cloud.exception.ArgumentParseException
import org.incendo.cloud.exception.CommandExecutionException
import org.incendo.cloud.exception.InvalidCommandSenderException
import org.incendo.cloud.exception.InvalidSyntaxException
import org.incendo.cloud.exception.NoPermissionException
import org.incendo.cloud.exception.parsing.NumberParseException
import org.incendo.cloud.parser.standard.BooleanParser
import org.incendo.cloud.parser.standard.EnumParser
import org.incendo.cloud.parser.standard.UUIDParser
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeoutException

class CommandExceptionHandler<C : CloudSender, M : AbstractCommandMessageConfig>(
    private val manager: CommandManager<C>,
    private val messages: ConfigurationFactory<M>,
) {

    private val logger = LoggerFactory.getLogger(CommandExceptionHandler::class.java)
    private val statusCodeSuffix = Regex("""\s*\(\d+\)$""")

    fun register() {
        manager.exceptionController()
            .registerHandler(CommandExecutionException::class.java) { context ->
                val cause = context.exception().cause ?: context.exception()
                send(context.context().sender(), messages().failed, tags("reason" to reason(cause)))
            }
            .registerHandler(InvalidSyntaxException::class.java) { context ->
                send(
                    context.context().sender(),
                    messages().syntax,
                    tags("command" to "/${context.exception().correctSyntax()}"),
                )
            }
            .registerHandler(NoPermissionException::class.java) { context ->
                send(context.context().sender(), messages().permission)
            }
            .registerHandler(InvalidCommandSenderException::class.java) { context ->
                send(context.context().sender(), messages().invalidSender)
            }
            .registerHandler(ArgumentParseException::class.java) { context ->
                handleArgument(context.context().sender(), context.exception().cause)
            }
    }

    private fun handleArgument(sender: C, cause: Throwable) {
        val argument = messages().argument
        when (cause) {
            is NumberParseException -> handleNumber(sender, cause)
            is BooleanParser.BooleanParseException -> send(sender, argument.invalidBoolean, tags("value" to cause.input()))
            is EnumParser.EnumParseException -> send(
                sender,
                argument.invalidEnum,
                tags(
                    "value" to cause.input(),
                    "options" to cause.enumClass().enumConstants.joinToString(", ") { it.name.lowercase() },
                ),
            )
            is UUIDParser.UUIDParseException -> send(sender, argument.invalidUuid, tags("value" to cause.input()))
            else -> send(sender, argument.invalid, tags("value" to (cause.message ?: "")))
        }
    }

    private fun handleNumber(sender: C, cause: NumberParseException) {
        val argument = messages().argument
        val number = cause.input().toBigDecimalOrNull()
        val range = cause.range()
        when {
            number == null -> send(sender, argument.invalidNumber, tags("value" to cause.input()))
            number < range.min().toString().toBigDecimal() -> send(sender, argument.numberTooSmall, tags("min" to range.min()))
            number > range.max().toString().toBigDecimal() -> send(sender, argument.numberTooLarge, tags("max" to range.max()))
            else -> send(sender, argument.invalidNumber, tags("value" to cause.input()))
        }
    }

    private fun reason(throwable: Throwable): String {
        val causes = generateSequence(throwable) { it.cause }
        val exception = causes.filterIsInstance<CloudApiException>().firstOrNull()

        return when {
            exception == null -> nonApiReason(throwable, causes)
            exception.reason == CloudApiException.Reason.UNAVAILABLE && exception.statusCode <= 0 -> "the controller is not reachable"
            exception.reason == CloudApiException.Reason.UNAVAILABLE -> "the controller is currently unavailable"
            else -> formatMessage(exception.message)
        }
    }

    private fun nonApiReason(throwable: Throwable, causes: Sequence<Throwable>): String {
        if (causes.any { it is TimeoutException }) return "the controller did not respond in time"
        logger.error("An unexpected error occurred while executing a command", throwable)
        return "an internal error occurred"
    }

    private fun formatMessage(error: String?): String {
        val message = error.orEmpty().replace(statusCodeSuffix, "").trim().trimEnd('.')
        if (message.isBlank()) return "an unknown error occurred"
        val isAcronym = message.length > 1 && message[1].isUpperCase()
        return if (isAcronym) message else message.replaceFirstChar { it.lowercase() }
    }

    private fun messages() = messages.get().command.exception

    private fun send(sender: C, message: String, vararg resolvers: TagResolver) {
        sender.sendMessage(messages.get().msg(message, *resolvers))
    }
}
