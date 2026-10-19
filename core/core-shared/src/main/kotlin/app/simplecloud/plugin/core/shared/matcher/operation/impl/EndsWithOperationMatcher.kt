package app.simplecloud.plugin.core.shared.matcher.operation.impl

import app.simplecloud.plugin.core.shared.matcher.operation.StringOperationMatcher
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
object EndsWithOperationMatcher : StringOperationMatcher {

    override fun matches(key: String, value: String): Boolean = key.endsWith(value, ignoreCase = true)
}
