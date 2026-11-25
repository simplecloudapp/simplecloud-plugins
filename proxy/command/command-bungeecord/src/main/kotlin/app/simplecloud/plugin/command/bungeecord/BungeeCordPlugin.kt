package app.simplecloud.plugin.command.bungeecord

import app.simplecloud.plugin.command.shared.CommandPlugin
import app.simplecloud.plugin.command.shared.command.CloudCommand
import app.simplecloud.core.bungeecord.command.BungeeCloudCommandManager
import net.md_5.bungee.api.plugin.Plugin

class BungeeCordPlugin : Plugin() {

    private val plugin = CommandPlugin(dataFolder)

    override fun onEnable() {
        CloudCommand(BungeeCloudCommandManager.create(this), plugin).register()
        plugin.start()
    }

    override fun onDisable() {
        plugin.shutdown()
    }
}
