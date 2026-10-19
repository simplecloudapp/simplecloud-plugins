package app.simplecloud.plugin.core.shared.matcher

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class ServerMatcherConfiguration(
    val operation: OperationType = OperationType.STARTS_WITH,
    val value: String = "",
    val negate: Boolean = false,
) {

    fun matches(name: String): Boolean = this.operation.matches(name, this.value, this.negate)

    fun anyMatches(names: List<String>): Boolean = this.operation.anyMatches(names, this.value, this.negate)

    fun allMatches(names: List<String>): Boolean = this.operation.allMatches(names, this.value, this.negate)
}
