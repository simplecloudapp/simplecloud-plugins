package app.simplecloud.plugin.core.shared.matcher.operation

interface OperationMatcher<K, V> {

    fun matches(key: K, value: V): Boolean
}
