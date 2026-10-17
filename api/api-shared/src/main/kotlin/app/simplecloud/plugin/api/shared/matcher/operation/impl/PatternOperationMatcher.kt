package app.simplecloud.plugin.api.shared.matcher.operation.impl

import app.simplecloud.plugin.api.shared.matcher.operation.StringOperationMatcher
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.util.regex.Pattern

@ConfigSerializable
object PatternOperationMatcher : StringOperationMatcher {

    override fun matches(key: String, value: String): Boolean {
        return Pattern.matches(key, value)
    }

}