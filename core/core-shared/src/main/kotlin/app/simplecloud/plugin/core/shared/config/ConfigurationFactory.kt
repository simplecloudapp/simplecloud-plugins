package app.simplecloud.plugin.core.shared.config

import app.simplecloud.plugin.core.shared.config.migration.ConfigMigrations
import java.io.File
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference

class ConfigurationFactory<E>(
    private val file: File,
    javaClass: Class<E>,
    migrations: ConfigMigrations? = null,
) {

    private val configurator = YamlFileConfigurator(javaClass, migrations)
    private val config = AtomicReference<E?>()

    fun loadOrCreate(defaultConfig: E): E {
        if (file.exists()) return load()

        save(defaultConfig)
        return defaultConfig
    }

    fun get(): E = checkNotNull(config.get()) { "${file.name} has not been loaded yet" }

    fun save(entry: E) {
        configurator.save(file, entry)
        config.set(entry)
    }

    fun reload() {
        load()
    }

    private fun load(): E {
        val loaded = checkNotNull(configurator.load(file)) { "Failed to load ${file.name}" }
        config.set(loaded)
        return loaded
    }

    companion object {
        inline fun <reified E> create(
            dataDirectory: File,
            fileName: String,
            default: E,
            migrations: ConfigMigrations? = null,
            classLoader: ClassLoader = E::class.java.classLoader,
        ): ConfigurationFactory<E> {
            val file = File(dataDirectory, fileName)
            DefaultConfigInstaller.install(file.toPath(), classLoader)
            val factory = ConfigurationFactory(file, E::class.java, migrations)
            factory.loadOrCreate(default)
            return factory
        }

        inline fun <reified E> create(
            dataDirectory: Path,
            fileName: String,
            default: E,
            migrations: ConfigMigrations? = null,
            classLoader: ClassLoader = E::class.java.classLoader,
        ): ConfigurationFactory<E> = create(dataDirectory.toFile(), fileName, default, migrations, classLoader)
    }
}
