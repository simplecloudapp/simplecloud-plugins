package app.simplecloud.plugin.core.shared.placeholder

import app.simplecloud.plugin.core.shared.placeholder.provider.GroupPlaceholderProvider
import app.simplecloud.plugin.core.shared.placeholder.provider.PersistentServerPlaceholderProvider
import app.simplecloud.plugin.core.shared.placeholder.provider.ServerPlaceholderProvider

object PlaceholderProvider {

    val serverPlaceholderProvider = ServerPlaceholderProvider()

    val groupPlaceholderProvider = GroupPlaceholderProvider()

    val persistentServerPlaceholderProvider = PersistentServerPlaceholderProvider()
}
