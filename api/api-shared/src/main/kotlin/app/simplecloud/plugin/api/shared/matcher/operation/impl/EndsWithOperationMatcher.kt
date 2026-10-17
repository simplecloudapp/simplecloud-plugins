package app.simplecloud.plugin.api.shared.matcher.operation.impl

import app.simplecloud.plugin.api.shared.matcher.operation.StringOperationMatcher
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
object EndsWithOperationMatcher : StringOperationMatcher {

    override fun matches(key: String, value: String): Boolean {
        return key.endsWith(value, ignoreCase = true)
    }

}