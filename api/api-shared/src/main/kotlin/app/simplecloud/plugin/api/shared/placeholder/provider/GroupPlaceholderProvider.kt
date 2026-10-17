package app.simplecloud.plugin.api.shared.placeholder.provider

import app.simplecloud.api.CloudApi
import app.simplecloud.api.group.Group
import app.simplecloud.plugin.api.shared.placeholder.argument.PropertiesArgumentsResolver
import app.simplecloud.plugin.api.shared.placeholder.argument.group.PlayerCountArgumentsResolver
import app.simplecloud.plugin.api.shared.placeholder.argument.group.ServerCountArgumentsResolver
import app.simplecloud.plugin.api.shared.placeholder.single.SingleGroupPlaceholderExecutor

class GroupPlaceholderProvider : AbstractPlaceholderProvider<Group>(
    SingleGroupPlaceholderExecutor()
) {

    override suspend fun getArgumentsResolvers(cloudApi: CloudApi, value: Group) = listOf(
        PropertiesArgumentsResolver(value.properties),
        PlayerCountArgumentsResolver(cloudApi, value),
        ServerCountArgumentsResolver(cloudApi, value)
    )

}
