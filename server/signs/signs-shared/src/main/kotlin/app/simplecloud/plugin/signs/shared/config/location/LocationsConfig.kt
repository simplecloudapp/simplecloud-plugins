package app.simplecloud.plugin.signs.shared.config.location

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class LocationsConfig(
    val group: String? = null,
    val persistentServer: String? = null,
    val locations: List<SignLocation> = emptyList(),
) {

    fun identifier(): String = group ?: persistentServer.orEmpty()

    fun isGroup(): Boolean = !group.isNullOrEmpty()

    fun isPersistentServer(): Boolean = !persistentServer.isNullOrEmpty()
}

@ConfigSerializable
data class SignLocation(
    val world: String = "",
    val x: Double = 0.0,
    val y: Double = 0.0,
    val z: Double = 0.0,
)
