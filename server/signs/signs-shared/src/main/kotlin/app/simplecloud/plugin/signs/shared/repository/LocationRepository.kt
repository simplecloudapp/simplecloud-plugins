package app.simplecloud.plugin.signs.shared.repository

import app.simplecloud.plugin.signs.shared.config.location.LocationsConfig
import app.simplecloud.plugin.signs.shared.config.location.SignLocation
import java.nio.file.Path

class LocationRepository(directory: Path) : YamlDirectory<LocationsConfig>(directory, LocationsConfig::class.java) {

    fun find(identifier: String): LocationsConfig? = findAll().find { it.identifier() == identifier }

    fun isRegistered(location: SignLocation): Boolean = findAll().any { location in it.locations }

    fun addToGroup(group: String, location: SignLocation) {
        val config = findAll().find { it.group == group }
        add(config ?: LocationsConfig(group = group), config, location)
    }

    fun addToPersistentServer(persistentServer: String, location: SignLocation) {
        val config = findAll().find { it.persistentServer == persistentServer }
        add(config ?: LocationsConfig(persistentServer = persistentServer), config, location)
    }

    fun remove(location: SignLocation) {
        val config = findAll().find { location in it.locations } ?: return
        save(fileName(config), config.copy(locations = config.locations - location), config)
    }

    fun removeAll(identifier: String): List<SignLocation> {
        val config = find(identifier) ?: return emptyList()
        save(fileName(config), config.copy(locations = emptyList()), config)
        return config.locations
    }

    private fun add(config: LocationsConfig, previous: LocationsConfig?, location: SignLocation) {
        save(fileName(config), config.copy(locations = config.locations + location), previous)
    }

    private fun fileName(config: LocationsConfig): String {
        val prefix = if (config.isPersistentServer()) "persistent-" else "group-"
        return "$prefix${config.identifier()}.yml"
    }
}
