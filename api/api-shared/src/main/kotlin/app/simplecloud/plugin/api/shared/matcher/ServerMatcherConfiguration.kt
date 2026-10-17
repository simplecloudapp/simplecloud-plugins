package app.simplecloud.plugin.api.shared.matcher

import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * @author Niklas Nieberler
 */

@ConfigSerializable
data class ServerMatcherConfiguration(
    val operation: OperationType = OperationType.STARTS_WITH,
    val value: String = "",
    val negate: Boolean = false,
) {

    fun matches(name: String): Boolean {
        return this.operation.matches(name, this.value, this.negate)
    }

    fun anyMatches(names: List<String>): Boolean {
        return this.operation.anyMatches(names, this.value, this.negate)
    }

    fun allMatches(names: List<String>): Boolean {
        return this.operation.allMatches(names, this.value, this.negate)
    }

}