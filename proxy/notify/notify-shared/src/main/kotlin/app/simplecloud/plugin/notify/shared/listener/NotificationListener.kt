package app.simplecloud.plugin.notify.shared.listener

import app.simplecloud.api.CloudApi
import app.simplecloud.api.event.Subscription
import app.simplecloud.api.server.ServerState
import app.simplecloud.plugin.core.shared.command.CloudSender
import app.simplecloud.plugin.core.shared.config.ConfigurationFactory
import app.simplecloud.plugin.core.shared.pretty.PrettifyCase
import app.simplecloud.plugin.core.shared.pretty.StringPrettifier
import app.simplecloud.plugin.notify.shared.config.MessageConfig
import app.simplecloud.plugin.notify.shared.notification.Notification
import app.simplecloud.plugin.notify.shared.notification.NotificationCache
import app.simplecloud.plugin.notify.shared.notification.NotificationType
import app.simplecloud.plugin.notify.shared.platform.NotifyPlatform
import app.simplecloud.plugin.notify.shared.utilities.MessageFormatter
import kotlinx.coroutines.*
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.util.concurrent.ConcurrentHashMap

class NotificationListener(
    private val api: CloudApi,
    private val messages: ConfigurationFactory<MessageConfig>,
    private val platform: NotifyPlatform,
    private val formatter: MessageFormatter,
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
                if (event.oldState == event.newState) {
                    return@onStateChanged
                }

                val server = event.server
                val state = event.newState

                scope.launch {
                    val type = NotificationType.fromServerState(state) ?: return@launch
                    val message = messages.get().notifications.server?.getByState(state) ?: return@launch
                    if (lastSentState.put(server.serverId, state) == state) return@launch

                    broadcast(
                        Notification(
                            formatter.server(server, message, Placeholder.unparsed("state", StringPrettifier.prettify(state.name, PrettifyCase.SENTENCE))),
                            type,
                        ),
                    )
                }
            },
        )

        subscriptions.add(
            api.event().server().onStopped { event ->
                val server = event.server

                scope.launch {
                    lastSentState.remove(server.serverId)
                    val message = messages.get().notifications.server?.stopped ?: return@launch
                    broadcast(
                        Notification(
                            formatter.server(server, message, Placeholder.unparsed("state", "Stopped")),
                            NotificationType.SERVER_STOPPED,
                        ),
                    )
                }
            },
        )
    }

    private fun subscribeGroupEvents() {
        subscriptions.add(
            api.event().group().onCreated { event ->
                val name = event.group?.name ?: event.serverGroupId

                scope.launch {
                    val message = messages.get().notifications.group?.created ?: return@launch

                    broadcast(
                        Notification(
                            formatter.group(message, name),
                            NotificationType.GROUP_CREATED,
                        ),
                    )
                }
            },
        )

        subscriptions.add(
            api.event().group().onDeleted { event ->
                val name = event.name

                scope.launch {
                    val message = messages.get().notifications.group?.deleted ?: return@launch
                    broadcast(
                        Notification(
                            formatter.group(message, name),
                            NotificationType.GROUP_DELETED,
                        ),
                    )
                }
            },
        )
    }

    private fun subscribePersistentServerEvents() {
        subscriptions.add(
            api.event().persistentServer().onCreated { event ->
                val name = event.name ?: event.persistentServerId

                scope.launch {
                    val message = messages.get().notifications.persistentServer?.created ?: return@launch
                    broadcast(
                        Notification(
                            formatter.persistentServer(message, name),
                            NotificationType.PERSISTENT_SERVER_CREATED,
                        ),
                    )
                }
            },
        )

        subscriptions.add(
            api.event().persistentServer().onDeleted { event ->
                val name = event.name ?: event.persistentServerId

                scope.launch {
                    val message = messages.get().notifications.persistentServer?.deleted ?: return@launch
                    broadcast(
                        Notification(
                            formatter.persistentServer(message, name),
                            NotificationType.PERSISTENT_SERVER_DELETED,
                        ),
                    )
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
