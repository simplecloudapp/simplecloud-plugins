package app.simplecloud.plugin.placeholders.paper

import app.simplecloud.plugin.placeholders.paper.expansion.PlaceholderApiExpansion
import app.simplecloud.plugin.placeholders.shared.PlaceholdersPlugin
import org.bukkit.plugin.java.JavaPlugin

class PaperPlaceholdersPlugin : JavaPlugin() {

    private val placeholders = PlaceholdersPlugin()

    override fun onEnable() {
        placeholders.start()
        PlaceholderApiExpansion(placeholders.resolver).register()
    }

    override fun onDisable() {
        placeholders.shutdown()
    }
}
