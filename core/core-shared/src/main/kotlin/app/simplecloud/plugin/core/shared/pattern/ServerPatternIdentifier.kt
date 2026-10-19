package app.simplecloud.plugin.core.shared.pattern

import app.simplecloud.api.CloudApi
import app.simplecloud.api.group.Group
import app.simplecloud.api.server.Server
import app.simplecloud.plugin.core.shared.pretty.StringPrettifier
import kotlinx.coroutines.future.await

class ServerPatternIdentifier(
    private val pattern: String = "<group_name>-<numerical_id>",
    regexPattern: String =
        pattern
            .replace("<group_name>", "(?<groupName>[a-zA-Z]+)")
            .replace("<numerical_id>", "(?<numericalId>\\d+)"),
    private val cloudApi: CloudApi = CloudApi.create(),
) {

    private val regex = Regex(regexPattern)

    fun parse(name: String, customRegex: Regex? = null): Pair<String, Int> {
        val matchResult = requireNotNull(customRegex?.matchEntire(name) ?: this.regex.matchEntire(name)) {
            "$name does not match the pattern"
        }
        val groupName = requireNotNull(matchResult.groups["groupName"]?.value) {
            "Group name not found"
        }
        val numericalId = requireNotNull(matchResult.groups["numericalId"]?.value?.toInt()) {
            "Numerical ID not found"
        }
        return Pair(groupName, numericalId)
    }

    fun parseServerToPattern(server: Server): String = this.pattern
        .replace("<group_name>", server.serverBase.name)
        .replace(
            "<group_pretty_name>",
            server.properties["pretty-name"]?.toString()
                ?: StringPrettifier.prettify(server.serverBase.name),
        )
        .replace("<id>", server.serverId)
        .replace("<unique_id>", server.serverId)
        .replace("<numerical_id>", server.numericalId.toString())

    suspend fun getGroup(name: String): Group? {
        val groupName = parse(name).first
        return try {
            this.cloudApi.group().getGroupByName(groupName).await()
        } catch (_: Exception) {
            null
        }
    }

    fun getNumericalId(name: String): Int = parse(name).second
}
