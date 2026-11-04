package app.simplecloud.plugin.signs.paper

import app.simplecloud.plugin.core.paper.command.PaperCloudSender
import app.simplecloud.plugin.signs.shared.command.SignTargetFinder
import app.simplecloud.plugin.signs.shared.config.location.SignLocation
import kotlinx.coroutines.future.await
import org.bukkit.FluidCollisionMode
import org.bukkit.Tag
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.CompletableFuture

class PaperSignTargetFinder(private val plugin: JavaPlugin) : SignTargetFinder<PaperCloudSender> {

    override suspend fun find(sender: PaperCloudSender): SignLocation? {
        val player = sender.getCommandSender() as? Player ?: return null
        val executor = plugin.server.scheduler.getMainThreadExecutor(plugin)
        return CompletableFuture.supplyAsync({ findSign(player) }, executor).await()
    }

    private fun findSign(player: Player): SignLocation? {
        val block = player.getTargetBlockExact(MAX_DISTANCE, FluidCollisionMode.NEVER) ?: return null
        if (!Tag.ALL_SIGNS.isTagged(block.type)) return null
        return PaperSignLocations.of(block)
    }

    companion object {
        private const val MAX_DISTANCE = 8
    }
}
