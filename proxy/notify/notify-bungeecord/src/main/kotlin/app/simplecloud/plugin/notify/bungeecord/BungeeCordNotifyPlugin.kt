package app.simplecloud.plugin.notify.bungeecord

import app.simplecloud.plugin.core.bungeecord.command.BungeeCloudCommandManager
import app.simplecloud.plugin.notify.bungeecord.listener.NotifyConnectionListener
import app.simplecloud.plugin.notify.bungeecord.platform.BungeeCordPlatformImpl
import app.simplecloud.plugin.notify.shared.NotifyPlugin
import app.simplecloud.plugin.notify.shared.command.NotifyCommand
import net.md_5.bungee.api.plugin.Plugin

class BungeeCordNotifyPlugin : Plugin() {

    private var notify: NotifyPlugin? = null

    override fun onEnable() {
        val notify = NotifyPlugin(BungeeCordPlatformImpl(this))
        this.notify = notify

        notify.startup()

        proxy.pluginManager.registerListener(this, NotifyConnectionListener(notify))

        registerCommands(notify)
    }

    override fun onDisable() {
        notify?.shutdown()
    }

    private fun registerCommands(notify: NotifyPlugin) {
        NotifyCommand(BungeeCloudCommandManager.create(this), notify).register()
    }
}
