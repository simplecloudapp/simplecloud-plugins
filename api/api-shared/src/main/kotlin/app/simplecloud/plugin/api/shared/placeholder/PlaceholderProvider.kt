package app.simplecloud.plugin.api.shared.placeholder

import app.simplecloud.plugin.api.shared.placeholder.provider.GroupPlaceholderProvider
import app.simplecloud.plugin.api.shared.placeholder.provider.PersistentServerPlaceholderProvider
import app.simplecloud.plugin.api.shared.placeholder.provider.ServerPlaceholderProvider

/**
 * @author Niklas Nieberler
 */

object PlaceholderProvider {

    val serverPlaceholderProvider = ServerPlaceholderProvider()

    val groupPlaceholderProvider = GroupPlaceholderProvider()

    val persistentServerPlaceholderProvider = PersistentServerPlaceholderProvider()

}