package app.simplecloud.plugin.core.shared.matcher.operation.impl

import app.simplecloud.plugin.core.shared.matcher.operation.StringOperationMatcher
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
object ContainsOperationMatcher : StringOperationMatcher {

    override fun matches(key: String, value: String): Boolean = key.contains(value, ignoreCase = true)
}
