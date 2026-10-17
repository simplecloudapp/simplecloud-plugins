package app.simplecloud.plugin.api.shared.placeholder.provider

import app.simplecloud.api.CloudApi
import app.simplecloud.api.persistentserver.PersistentServer
import app.simplecloud.plugin.api.shared.placeholder.argument.*
import app.simplecloud.plugin.api.shared.placeholder.single.SinglePersistentServerPlaceholderExecutor

/**
 * @author Niklas Nieberler
 */

class PersistentServerPlaceholderProvider : AbstractPlaceholderProvider<PersistentServer>(
    SinglePersistentServerPlaceholderExecutor()
) {

    override suspend fun getArgumentsResolvers(cloudApi: CloudApi, value: PersistentServer) = listOf(
        PropertiesArgumentsResolver(value.properties)
    )

}