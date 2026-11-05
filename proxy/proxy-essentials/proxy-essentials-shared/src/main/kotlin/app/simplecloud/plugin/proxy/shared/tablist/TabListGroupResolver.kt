package app.simplecloud.plugin.proxy.shared.tablist

import app.simplecloud.plugin.proxy.shared.config.TabListGroup

object TabListGroupResolver {

    fun resolve(groups: List<TabListGroup>, serverName: String): TabListGroup? {
        val namedGroups = groups.filter { it.serverNameMatcher == null }
        return groups.find { it.serverNameMatcher?.matches(serverName) == true }
            ?: namedGroups.find { it.name.equals(serverName, ignoreCase = true) }
            ?: namedGroups.find { serverName.startsWith(it.name, ignoreCase = true) }
            ?: namedGroups.find { it.name == "*" }
            ?: namedGroups.find { it.name.equals("global", ignoreCase = true) }
    }
}
