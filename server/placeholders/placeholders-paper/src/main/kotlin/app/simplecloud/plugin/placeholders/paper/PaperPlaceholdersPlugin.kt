package app.simplecloud.plugin.placeholders.paper

import app.simplecloud.plugin.placeholders.paper.expansion.MiniPlaceholdersExpansion
import app.simplecloud.plugin.placeholders.paper.expansion.PlaceholderApiExpansion
import app.simplecloud.plugin.placeholders.shared.PlaceholdersPlugin
import org.bukkit.plugin.java.JavaPlugin

class PaperPlaceholdersPlugin : JavaPlugin() {

    private val placeholders = PlaceholdersPlugin()

    private var placeholderApiExpansion: PlaceholderApiExpansion? = null
    private var miniPlaceholdersExpansion: MiniPlaceholdersExpansion? = null

    override fun onEnable() {
        placeholders.start()

        if (server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            val expansion = PlaceholderApiExpansion(placeholders.resolver)
            expansion.register()
            placeholderApiExpansion = expansion
        }
        if (server.pluginManager.isPluginEnabled("MiniPlaceholders")) {
            val expansion = MiniPlaceholdersExpansion(placeholders.resolver, placeholders.types)
            expansion.register()
            miniPlaceholdersExpansion = expansion
        }

        if (placeholderApiExpansion == null && miniPlaceholdersExpansion == null) {
            logger.warning("Neither PlaceholderAPI nor MiniPlaceholders is installed, no placeholders will be registered")
        }
    }

    override fun onDisable() {
        placeholderApiExpansion?.unregister()
        miniPlaceholdersExpansion?.unregister()
        placeholders.shutdown()
    }
}
