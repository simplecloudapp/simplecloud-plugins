package app.simplecloud.plugin.signs.paper.listener

import app.simplecloud.plugin.signs.paper.PaperSignLocations
import app.simplecloud.plugin.signs.shared.SignManager
import com.google.common.io.ByteStreams
import org.bukkit.Tag
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.java.JavaPlugin

class SignListener(
    private val plugin: JavaPlugin,
    private val signs: SignManager,
) : Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        if (!Tag.ALL_SIGNS.isTagged(block.type)) return
        val location = PaperSignLocations.of(block)
        if (!signs.isRegistered(location)) return

        event.isCancelled = true
        if (event.hand != EquipmentSlot.HAND) return
        val server = signs.serverAt(location) ?: return

        val message = ByteStreams.newDataOutput()
        message.writeUTF("Connect")
        message.writeUTF(signs.proxyName(server))
        event.player.sendPluginMessage(plugin, CHANNEL, message.toByteArray())
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        if (!Tag.ALL_SIGNS.isTagged(event.block.type)) return
        if (signs.isRegistered(PaperSignLocations.of(event.block))) {
            event.isCancelled = true
        }
    }

    companion object {
        const val CHANNEL = "BungeeCord"
    }
}
