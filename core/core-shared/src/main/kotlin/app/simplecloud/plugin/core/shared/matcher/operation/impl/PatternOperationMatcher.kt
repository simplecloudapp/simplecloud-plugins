package app.simplecloud.plugin.core.shared.matcher.operation.impl

import app.simplecloud.plugin.core.shared.matcher.operation.StringOperationMatcher
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.util.regex.Pattern

@ConfigSerializable
object PatternOperationMatcher : StringOperationMatcher {

    override fun matches(key: String, value: String): Boolean = Pattern.matches(value, key)
}
