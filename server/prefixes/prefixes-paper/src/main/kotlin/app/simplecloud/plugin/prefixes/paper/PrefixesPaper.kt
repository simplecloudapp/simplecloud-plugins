package app.simplecloud.plugin.prefixes.paper

import app.simplecloud.plugin.core.paper.command.PaperCloudCommandManager
import app.simplecloud.plugin.prefixes.api.PrefixesApi
import app.simplecloud.plugin.prefixes.paper.display.PaperDisplayManager
import app.simplecloud.plugin.prefixes.paper.display.PaperTablist
import app.simplecloud.plugin.prefixes.paper.listener.PlayerListener
import app.simplecloud.plugin.prefixes.paper.platform.PaperPlatformImpl
import app.simplecloud.plugin.prefixes.paper.platform.PaperPrefixesListener
import app.simplecloud.plugin.prefixes.shared.Prefixes
import app.simplecloud.plugin.prefixes.shared.command.PrefixesCommand
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import space.chunks.customname.paper.CustomNameManagerImpl
import java.util.concurrent.TimeUnit

class PrefixesPaper : JavaPlugin() {

    private var prefixes: Prefixes? = null

    override fun onEnable() {
        val customNameManager = CustomNameManagerImpl(this)
        customNameManager.registerListeners()

        val prefixes = Prefixes(PaperPlatformImpl(this))
        this.prefixes = prefixes

        prefixes.startup()

        Bukkit.getServicesManager().register(PrefixesApi::class.java, prefixes.api, this, ServicePriority.Normal)

        val manager = PaperDisplayManager(this, prefixes, customNameManager)
        val tablist = PaperTablist()
        Bukkit.getPluginManager().registerEvents(PlayerListener(prefixes, manager, tablist), this)

        registerSync(prefixes, manager, tablist)
        Bukkit.getAsyncScheduler().runAtFixedRate(this, { manager.refreshVanished() }, 1L, 1L, TimeUnit.SECONDS)

        prefixes.addListener(PaperPrefixesListener(this, prefixes, manager, tablist))

        registerCommands(prefixes)
        prefixes.api.refreshAll()
    }

    override fun onDisable() {
        prefixes?.shutdown()
    }

    private fun registerSync(prefixes: Prefixes, manager: PaperDisplayManager, tablist: PaperTablist) {
        val sync = prefixes.sync ?: return

        sync.subscriber.subscribeChatMessage { message -> Bukkit.getServer().sendMessage(message) }
        sync.subscriber.subscribeTablist(tablist::update, tablist::remove) { manager.publishAll() }
        sync.publisher.publishTablistRequest()

        Bukkit.getAsyncScheduler().runAtFixedRate(this, {
            manager.publishAll()
            tablist.expire()
        }, 30L, 30L, TimeUnit.SECONDS)
    }

    private fun registerCommands(prefixes: Prefixes) {
        PrefixesCommand(PaperCloudCommandManager.create(this), prefixes).register()
    }
}
