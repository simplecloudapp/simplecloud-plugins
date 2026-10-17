package app.simplecloud.plugin.notify.velocity

import app.simplecloud.plugin.notify.shared.NotifyPlugin
import app.simplecloud.plugin.notify.shared.command.NotifyCommand
import app.simplecloud.plugin.notify.velocity.command.VelocityCloudSender
import app.simplecloud.plugin.notify.velocity.listener.NotifyConnectionListener
import app.simplecloud.plugin.notify.velocity.platform.VelocityPlatformImpl
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.velocity.VelocityCommandManager
import java.nio.file.Path

@Plugin(
    id = "simplecloud-notify",
    name = "simplecloud-notify",
    version = BuildConstants.VERSION,
    authors = ["rlqu"],
    description = "An ingame plugin that notifies about v3 server changes",
    url = "https://github.com/simplecloudapp/notify-plugin",
    dependencies = [
        Dependency("simplecloud-api")
    ]
)
class VelocityNotifyPlugin @Inject constructor(
    @DataDirectory private val dataDirectory: Path,
    private val server: ProxyServer,
) {

    private var notify: NotifyPlugin? = null

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        val notify = NotifyPlugin(VelocityPlatformImpl(dataDirectory, server))
        this.notify = notify

        notify.startup()

        server.eventManager.register(this, NotifyConnectionListener(notify))

        registerCommands(notify)
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        notify?.shutdown()
    }

    private fun registerCommands(notify: NotifyPlugin) {
        val manager = VelocityCommandManager(
            server.pluginManager.ensurePluginContainer(this),
            server,
            ExecutionCoordinator.asyncCoordinator(),
            SenderMapper.create(::VelocityCloudSender, VelocityCloudSender::source)
        )

        NotifyCommand(manager, notify).register()
    }

}
