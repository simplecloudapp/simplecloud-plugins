package app.simplecloud.plugin.core.shared.config

import app.simplecloud.plugin.core.shared.config.migration.ConfigMigrator
import java.io.File
import java.nio.file.Path

class ConfigurationFactory<E>(private val file: File, javaClass: Class<E>, configMigrator: ConfigMigrator? = null) {

    private val configurator = YamlFileConfigurator(javaClass, configMigrator)

    private var config: E? = null

    /** Loads the config, or creates the file from [defaultConfig] if it does not exist. */
    fun loadOrCreate(defaultConfig: E): E {
        if (file.exists()) {
            return load() ?: throw NullPointerException("failed to load config")
        }

        save(defaultConfig)
        return defaultConfig
    }

    /** The cached config. Throws if it has not been loaded yet. */
    fun get(): E = config ?: throw NullPointerException("failed to find config")

    /** Writes the [entry] to the file and caches it. */
    fun save(entry: E) {
        configurator.save(file, entry)
        config = entry
    }

    /** Reloads the file into the cache. */
    fun reload() {
        load()
    }

    private fun load(): E? {
        val loaded = configurator.load(file)
        config = loaded
        return loaded
    }

    companion object {

        /**
         * Installs the bundled default file if missing, creates the factory and loads the config.
         *
         * The bundled file is looked up by [fileName] with the class loader of the config class.
         */
        inline fun <reified E> create(
            dataDirectory: File,
            fileName: String,
            default: E,
            migrator: ConfigMigrator? = null,
            classLoader: ClassLoader = E::class.java.classLoader,
        ): ConfigurationFactory<E> {
            val file = File(dataDirectory, fileName)
            DefaultConfigInstaller.install(file.toPath(), classLoader)
            val factory = ConfigurationFactory(file, E::class.java, migrator)
            factory.loadOrCreate(default)
            return factory
        }

        /** Same as [create] but with a [Path] as data directory. */
        inline fun <reified E> create(
            dataDirectory: Path,
            fileName: String,
            default: E,
            migrator: ConfigMigrator? = null,
            classLoader: ClassLoader = E::class.java.classLoader,
        ): ConfigurationFactory<E> = create(dataDirectory.toFile(), fileName, default, migrator, classLoader)
    }
}
