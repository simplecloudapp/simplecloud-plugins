package app.simplecloud.plugin.api.shared.config

import java.io.File

/**
 * @author Niklas Nieberler
 */

class ConfigurationFactory<E> @JvmOverloads constructor(
    private val file: File,
    javaClass: Class<E>,
    configMigrator: ConfigMigrator? = null,
) {

    private val yamlFileConfigurator = YamlFileConfigurator(javaClass, configMigrator)

    private var config: E? = null

    /**
     * Loads the config and if it does not exist, it creates a default config
     * @param defaultConfig the default configuration
     */
    fun loadOrCreate(defaultConfig: E): E {
        if (this.file.exists()) {
            return loadConfiguration()
                ?: throw NullPointerException("failed to load config")
        }

        this.yamlFileConfigurator.save(this.file, defaultConfig)
        this.config = defaultConfig
        return defaultConfig
    }

    /**
     * Gets the cached config file
     */
    fun get(): E {
        return this.config ?: throw NullPointerException("failed to find config")
    }

    fun save(entry: E) {
        this.yamlFileConfigurator.save(this.file, entry)
        this.config = entry
    }

    private fun loadConfiguration(): E? {
        val configuration = this.yamlFileConfigurator.load(this.file)
        this.config = configuration
        return configuration
    }

    fun reload() {
        loadConfiguration()
    }
}
