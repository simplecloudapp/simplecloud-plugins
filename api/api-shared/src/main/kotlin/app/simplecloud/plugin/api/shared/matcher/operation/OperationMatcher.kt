package app.simplecloud.plugin.api.shared.matcher.operation

interface OperationMatcher<K, V> {

    fun matches(key: K, value: V): Boolean

}