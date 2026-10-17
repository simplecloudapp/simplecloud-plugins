package app.simplecloud.plugin.notify.shared.command

import app.simplecloud.plugin.notify.shared.notification.NotificationType
import org.incendo.cloud.kotlin.coroutines.SuspendingSuggestionProvider
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider

object NotifySuggestions {

    fun <C : CloudSender> receivableTypes(): SuggestionProvider<C> =
        SuspendingSuggestionProvider<C> { context, _ ->
            NotificationType.entries
                .filter { type -> type.canReceive(context.sender()) }
                .map { type -> Suggestion.suggestion(type.key) }
        }.asSuggestionProvider()

    fun <C : CloudSender> types(): SuggestionProvider<C> =
        SuspendingSuggestionProvider<C> { _, _ ->
            NotificationType.entries.map { type -> Suggestion.suggestion(type.key) }
        }.asSuggestionProvider()
}
