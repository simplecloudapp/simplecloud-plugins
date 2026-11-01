package app.simplecloud.plugin.signs.paper

import app.simplecloud.plugin.signs.shared.SignManager
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.plugin.java.JavaPlugin

class PaperSignsBootstrap : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {
        SignManager.installDefaults(context.dataDirectory)
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin = PaperSignsPlugin()
}
