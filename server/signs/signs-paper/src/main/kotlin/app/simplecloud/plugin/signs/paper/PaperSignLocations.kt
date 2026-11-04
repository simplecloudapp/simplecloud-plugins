package app.simplecloud.plugin.signs.paper

import app.simplecloud.plugin.signs.shared.config.location.SignLocation
import org.bukkit.Bukkit
import org.bukkit.block.Block

object PaperSignLocations {

    fun of(block: Block): SignLocation =
        SignLocation(block.world.name, block.x.toDouble(), block.y.toDouble(), block.z.toDouble())

    fun blockAt(location: SignLocation): Block? {
        val world = Bukkit.getWorld(location.world) ?: return null
        return world.getBlockAt(location.x.toInt(), location.y.toInt(), location.z.toInt())
    }
}
