package app.simplecloud.plugin.notify.shared.listener

import app.simplecloud.api.CloudApi
import app.simplecloud.api.event.Subscription
import app.simplecloud.api.event.server.ServerStoppedEvent
import app.simplecloud.api.server.ServerState
import app.simplecloud.core.shared.command.CloudSender
import app.simplecloud.core.shared.config.ConfigurationFactory
import app.simplecloud.core.shared.pretty.PrettifyCase
import app.simplecloud.core.shared.pretty.StringPrettifier
import app.simplecloud.plugin.notify.shared.config.MessageConfig
import app.simplecloud.plugin.notify.shared.notification.Notification
import app.simplecloud.plugin.notify.shared.notification.NotificationCache
import app.simplecloud.plugin.notify.shared.notification.NotificationType
import app.simplecloud.plugin.notify.shared.platform.NotifyPlatform
import app.simplecloud.plugin.notify.shared.utilities.MessageFormatter
import kotlinx.coroutines.*
import kotlinx.coroutines.future.*
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.util.concurrent.ConcurrentHashMap

class NotificationListener(
    private val api: CloudApi,
    private val messages: ConfigurationFactory<MessageConfig>,
    private val platform: NotifyPlatform,
    private val cache: NotificationCache,
) {

    private val scope = CoroutineScope(Dispatchers.IO.limitedParallelism(1) + SupervisorJob())
    private val subscriptions: MutableList<Subscription> = mutableListOf()
    private val lastSentState: MutableMap<String, ServerState> = ConcurrentHashMap()

    fun start() {
        subscribeServerEvents()
        subscribeGroupEvents()
        subscribePersistentServerEvents()
    }

    fun stop() {
        subscriptions.forEach { subscription -> subscription.close() }
        subscriptions.clear()
        lastSentState.clear()
        scope.cancel()
    }

    private fun subscribeServerEvents() {
        subscriptions.add(
            api.event().server().onStateChanged { event ->
                if (event.oldState == event.newState) return@onStateChanged

                val server = event.server
                val state = event.newState

                scope.launch {
                    val type = NotificationType.fromServerState(state) ?: return@launch
                    val config = messages.get()
                    val message = config.notifications.server?.getByState(state) ?: return@launch
                    if (lastSentState.put(server.serverId, state) == state) return@launch

                    val stateName = StringPrettifier.prettify(state.name, PrettifyCase.SENTENCE)
                    val component = MessageFormatter.server(
                        config,
                        server,
                        message,
                        Placeholder.unparsed("state", stateName),
                    )
                    broadcast(Notification(component, type))
                }
            },
        )

        subscriptions.add(
            api.event().server().onStopped { event ->
                scope.launch {
                    lastSentState.remove(event.serverId)
                    onStopped(event)
                }
            },
        )

        subscriptions.add(
            api.event().server().onDeleted { event ->
                scope.launch {
                    lastSentState.remove(event.serverId)
                }
            },
        )
    }

    private fun onStopped(event: ServerStoppedEvent) {
        val config = messages.get()
        val notifications = config.notifications.server ?: return
        val type = if (event.isCrashed) NotificationType.SERVER_CRASHED else NotificationType.SERVER_STOPPED
        val message = (if (event.isCrashed) notifications.crashed else notifications.stopped) ?: return
        broadcast(Notification(MessageFormatter.stoppedServer(config, event, message), type))
    }

    private fun subscribeGroupEvents() {
        subscriptions.add(
            api.event().group().onCreated { event ->
                val groupId = event.serverGroupId

                scope.launch {
                    val config = messages.get()
                    val message = config.notifications.group?.created ?: return@launch
                    val name = try {
                        api.group().getGroupById(groupId).await().name
                    } catch (_: Exception) {
                        groupId
                    }
                    val component = MessageFormatter.group(config, message, name)
                    broadcast(Notification(component, NotificationType.GROUP_CREATED))
                }
            },
        )

        subscriptions.add(
            api.event().group().onDeleted { event ->
                val name = event.name ?: event.serverGroupId

                scope.launch {
                    val config = messages.get()
                    val message = config.notifications.group?.deleted ?: return@launch
                    val component = MessageFormatter.group(config, message, name)
                    broadcast(Notification(component, NotificationType.GROUP_DELETED))
                }
            },
        )
    }

    private fun subscribePersistentServerEvents() {
        subscriptions.add(
            api.event().persistentServer().onCreated { event ->
                val name = event.name ?: event.persistentServerId

                scope.launch {
                    val config = messages.get()
                    val message = config.notifications.persistentServer?.created ?: return@launch
                    val component = MessageFormatter.persistentServer(config, message, name)
                    broadcast(Notification(component, NotificationType.PERSISTENT_SERVER_CREATED))
                }
            },
        )

        subscriptions.add(
            api.event().persistentServer().onDeleted { event ->
                val name = event.name ?: event.persistentServerId

                scope.launch {
                    val config = messages.get()
                    val message = config.notifications.persistentServer?.deleted ?: return@launch
                    val component = MessageFormatter.persistentServer(config, message, name)
                    broadcast(Notification(component, NotificationType.PERSISTENT_SERVER_DELETED))
                }
            },
        )
    }

    private fun broadcast(notification: Notification) {
        platform.getOnlinePlayers()
            .filter { sender -> isEnabled(sender, notification.type) }
            .forEach(notification::sendTo)
    }

    private fun isEnabled(sender: CloudSender, type: NotificationType): Boolean {
        val id = sender.getUniqueId() ?: return true
        return cache.isEnabled(id, type)
    }
}
