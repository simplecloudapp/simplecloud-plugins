package app.simplecloud.plugin.api.shared.placeholder.argument

import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue

/**
 * @author Niklas Nieberler
 */

class PropertiesArgumentsResolver(
    private val properties: Map<String, Any>
) : ArgumentsResolver {

    override fun getKey() = "property"

    override suspend fun resolve(arguments: ArgumentQueue): Tag? {
        val argumentName = arguments.popOr("property expected").value()
        val defaultArgument = arguments.peek()?.value() ?: ""
        val string = this.properties[argumentName] ?: defaultArgument
        return Tag.preProcessParsed(string.toString())
    }

}