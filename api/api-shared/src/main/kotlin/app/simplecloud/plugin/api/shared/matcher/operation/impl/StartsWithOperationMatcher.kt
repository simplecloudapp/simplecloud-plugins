package app.simplecloud.plugin.api.shared.matcher.operation.impl

import app.simplecloud.plugin.api.shared.matcher.operation.StringOperationMatcher
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
object StartsWithOperationMatcher : StringOperationMatcher {

    override fun matches(key: String, value: String): Boolean {
        return key.startsWith(value, ignoreCase = true)
    }

}