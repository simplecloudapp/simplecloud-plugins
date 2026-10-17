package app.simplecloud.plugin.api.shared.matcher.operation.impl

import app.simplecloud.plugin.api.shared.matcher.operation.StringOperationMatcher
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
object RegexOperationMatcher : StringOperationMatcher {

    override fun matches(key: String, value: String): Boolean {
        return Regex(value).matches(key)
    }

}