package app.simplecloud.plugin.api.shared.placeholder.provider

import app.simplecloud.api.CloudApi
import app.simplecloud.api.server.Server
import app.simplecloud.plugin.api.shared.placeholder.argument.*
import app.simplecloud.plugin.api.shared.placeholder.single.SingleServerPlaceholderExecutor

/**
 * @author Niklas Nieberler
 */

class ServerPlaceholderProvider : AbstractPlaceholderProvider<Server>(
    SingleServerPlaceholderExecutor()
) {

    override suspend fun getArgumentsResolvers(cloudApi: CloudApi, value: Server) = listOf(
        PropertiesArgumentsResolver(value.properties)
    )

}