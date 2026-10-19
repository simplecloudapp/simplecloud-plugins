package app.simplecloud.plugin.core.shared.config.serializer

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

object GenericEnumSerializer : TypeSerializer<Enum<*>> {

    override fun deserialize(type: Type, node: ConfigurationNode): Enum<*> {
        if (type !is Class<*> || !type.isEnum) {
            throw SerializationException("Type is not an enum class")
        }

        val value = node.string
        return type.enumConstants.filterIsInstance<Enum<*>>().firstOrNull { it.name == value }
            ?: throw SerializationException("Invalid enum constant: $value")
    }

    override fun serialize(type: Type, obj: Enum<*>?, node: ConfigurationNode) {
        node.set(obj?.name)
    }
}
