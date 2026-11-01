package app.simplecloud.plugin.signs.paper

import app.simplecloud.api.CloudApi
import app.simplecloud.api.CloudApiOptions
import app.simplecloud.plugin.core.paper.command.PaperCloudCommandManager
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.signs.paper.listener.SignListener
import app.simplecloud.plugin.signs.shared.SignManager
import app.simplecloud.plugin.signs.shared.command.SignCommand
import app.simplecloud.plugin.signs.shared.config.MessageConfig
import org.bukkit.plugin.java.JavaPlugin

class PaperSignsPlugin : JavaPlugin() {

    private val api = CloudApi.create(CloudApiOptions.builder().disableCache().build())
    private val signs = SignManager(api, dataPath, PaperSignRenderer(this))

    override fun onEnable() {
        val messages = ConfigurationFactory.create(dataPath, "messages.yml", MessageConfig())
        signs.start()
        server.scheduler.runTaskTimer(this, Runnable { signs.tick() }, 1L, 1L)

        server.messenger.registerOutgoingPluginChannel(this, SignListener.CHANNEL)
        server.pluginManager.registerEvents(SignListener(this, signs), this)

        val manager = PaperCloudCommandManager.create(this)
        SignCommand(manager, signs, messages, PaperSignTargetFinder(this)).register()
    }

    override fun onDisable() {
        signs.stop()
        api.close()
    }
}
