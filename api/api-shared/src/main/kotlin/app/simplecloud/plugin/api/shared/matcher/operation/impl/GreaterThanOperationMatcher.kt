package app.simplecloud.plugin.api.shared.matcher.operation.impl

import app.simplecloud.plugin.api.shared.matcher.operation.NumericOperationMatcher

object GreaterThanOperationMatcher : NumericOperationMatcher {

    override fun matches(key: Int, value: Int): Boolean {
        return key > value
    }

}