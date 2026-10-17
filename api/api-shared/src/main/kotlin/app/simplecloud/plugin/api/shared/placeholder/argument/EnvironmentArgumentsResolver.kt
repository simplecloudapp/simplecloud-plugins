package app.simplecloud.plugin.api.shared.placeholder.argument

import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue

/**
 * @author Niklas Nieberler
 */

class EnvironmentArgumentsResolver : ArgumentsResolver {

    override fun getKey() = "env"

    override suspend fun resolve(arguments: ArgumentQueue): Tag? {
        val argumentName = arguments.popOr("environment expected").value()
        val defaultArgument = arguments.peek()?.value() ?: ""
        val string = System.getenv(argumentName) ?: defaultArgument
        return Tag.preProcessParsed(string)
    }

}