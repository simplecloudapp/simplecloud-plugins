package app.simplecloud.plugin.placeholders.paper.expansion

import app.simplecloud.plugin.placeholders.shared.resolve.PlaceholderResolver
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class PlaceholderApiExpansion(
    private val resolver: PlaceholderResolver,
) : PlaceholderExpansion() {

    override fun getIdentifier() = "simplecloud"

    override fun getAuthor() = "SimpleCloud"

    override fun getVersion() = "0.0.0"

    override fun persist() = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? = resolver.resolve(params)
}
