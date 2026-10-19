package app.simplecloud.plugin.core.shared.matcher.operation.impl

import app.simplecloud.plugin.core.shared.matcher.operation.NumericOperationMatcher

object GreaterThanOperationMatcher : NumericOperationMatcher {

    override fun matches(key: Int, value: Int): Boolean = key > value
}
