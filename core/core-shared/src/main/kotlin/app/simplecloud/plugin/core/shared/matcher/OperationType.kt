package app.simplecloud.plugin.core.shared.matcher

import app.simplecloud.plugin.core.shared.matcher.operation.NumericOperationMatcher
import app.simplecloud.plugin.core.shared.matcher.operation.OperationMatcher
import app.simplecloud.plugin.core.shared.matcher.operation.StringOperationMatcher
import app.simplecloud.plugin.core.shared.matcher.operation.impl.ContainsOperationMatcher
import app.simplecloud.plugin.core.shared.matcher.operation.impl.EndsWithOperationMatcher
import app.simplecloud.plugin.core.shared.matcher.operation.impl.EqualsOperationMatcher
import app.simplecloud.plugin.core.shared.matcher.operation.impl.GreaterThanOperationMatcher
import app.simplecloud.plugin.core.shared.matcher.operation.impl.PatternOperationMatcher
import app.simplecloud.plugin.core.shared.matcher.operation.impl.RegexOperationMatcher
import app.simplecloud.plugin.core.shared.matcher.operation.impl.StartsWithOperationMatcher

enum class OperationType(private val matcher: OperationMatcher<*, *>) {

    REGEX(RegexOperationMatcher),
    PATTERN(PatternOperationMatcher),
    EQUALS(EqualsOperationMatcher),
    CONTAINS(ContainsOperationMatcher),
    STARTS_WITH(StartsWithOperationMatcher),
    ENDS_WITH(EndsWithOperationMatcher),
    GREATER_THAN(GreaterThanOperationMatcher),
    ;

    fun matches(key: Any, value: Any, negate: Boolean): Boolean {
        val matches =
            when (matcher) {
                is StringOperationMatcher -> {
                    if (key is String && value is String) {
                        matcher.matches(key, value)
                    } else {
                        false
                    }
                }

                is NumericOperationMatcher -> {
                    if (key is Int && value is Int) {
                        matcher.matches(key, value)
                    } else {
                        false
                    }
                }

                else -> false
            }

        return if (negate) matches.not() else matches
    }

    fun anyMatches(names: List<String>, value: String, negate: Boolean): Boolean = names.any {
        matches(it, value, negate)
    }

    fun allMatches(names: List<String>, value: String, negate: Boolean): Boolean = names.all {
        matches(it, value, negate)
    }
}
