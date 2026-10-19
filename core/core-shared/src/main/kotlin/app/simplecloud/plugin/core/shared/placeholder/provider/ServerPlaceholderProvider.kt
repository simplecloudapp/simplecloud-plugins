package app.simplecloud.plugin.core.shared.placeholder.provider

import app.simplecloud.api.CloudApi
import app.simplecloud.api.server.Server
import app.simplecloud.plugin.core.shared.placeholder.argument.PropertiesArgumentsResolver
import app.simplecloud.plugin.core.shared.placeholder.single.SingleServerPlaceholderExecutor

class ServerPlaceholderProvider :
    AbstractPlaceholderProvider<Server>(
        SingleServerPlaceholderExecutor(),
    ) {

    override suspend fun getArgumentsResolvers(cloudApi: CloudApi, value: Server) =
        listOf(
            PropertiesArgumentsResolver(value.properties),
        )
}
