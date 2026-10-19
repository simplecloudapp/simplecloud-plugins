package app.simplecloud.plugin.core.shared.placeholder.provider

import app.simplecloud.api.CloudApi
import app.simplecloud.api.persistentserver.PersistentServer
import app.simplecloud.plugin.core.shared.placeholder.argument.PropertiesArgumentsResolver
import app.simplecloud.plugin.core.shared.placeholder.single.SinglePersistentServerPlaceholderExecutor

class PersistentServerPlaceholderProvider :
    AbstractPlaceholderProvider<PersistentServer>(
        SinglePersistentServerPlaceholderExecutor(),
    ) {

    override suspend fun getArgumentsResolvers(cloudApi: CloudApi, value: PersistentServer) =
        listOf(
            PropertiesArgumentsResolver(value.properties),
        )
}
