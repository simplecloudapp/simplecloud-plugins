package app.simplecloud.plugin.command.bungeecord

import app.simplecloud.plugin.command.shared.CommandPlugin
import app.simplecloud.plugin.command.shared.command.CloudCommandHandler
import app.simplecloud.plugin.core.bungeecord.command.BungeeCloudCommandManager
import net.md_5.bungee.api.plugin.Plugin

class BungeeCordPlugin : Plugin() {

    private val plugin = CommandPlugin(dataFolder)

    override fun onEnable() {
        plugin.startup()
        CloudCommandHandler(BungeeCloudCommandManager.create(this), plugin).register()
    }

    override fun onDisable() {
        plugin.shutdown()
    }
}
