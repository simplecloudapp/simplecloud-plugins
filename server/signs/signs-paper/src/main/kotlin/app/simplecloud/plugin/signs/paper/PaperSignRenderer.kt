package app.simplecloud.plugin.signs.paper

import app.simplecloud.plugin.signs.shared.RenderedSign
import app.simplecloud.plugin.signs.shared.SignRenderer
import app.simplecloud.plugin.signs.shared.config.location.SignLocation
import net.kyori.adventure.text.Component
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.block.data.Directional
import org.bukkit.block.sign.Side
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.LoggerFactory

class PaperSignRenderer(private val plugin: JavaPlugin) : SignRenderer {

    private val logger = LoggerFactory.getLogger(PaperSignRenderer::class.java)

    override fun render(location: SignLocation, sign: RenderedSign) {
        val state = PaperSignLocations.blockAt(location)?.state as? Sign ?: return
        updateBehindBlock(state, sign.behindBlock)
        writeLines(state, sign.lines, sign.glowingText)
    }

    override fun clear(location: SignLocation) {
        runOnMainThread {
            val state = PaperSignLocations.blockAt(location)?.state as? Sign ?: return@runOnMainThread
            writeLines(state, emptyList(), false)
        }
    }

    private fun runOnMainThread(task: () -> Unit) {
        if (!plugin.isEnabled) return
        plugin.server.scheduler.runTask(plugin, Runnable { task() })
    }

    private fun writeLines(sign: Sign, lines: List<Component>, glowingText: Boolean) {
        Side.entries.forEach { side ->
            val signSide = sign.getSide(side)
            signSide.isGlowingText = glowingText
            // with the default black dye, glowing text gets a light outline that washes out light text colors
            if (glowingText) signSide.color = DyeColor.GRAY
            repeat(LINE_COUNT) { index -> signSide.line(index, lines.getOrElse(index) { Component.empty() }) }
        }
        sign.update()
    }

    private fun updateBehindBlock(sign: Sign, materialName: String?) {
        if (materialName == null) return
        val material = Material.matchMaterial(materialName)
        if (material == null || !material.isBlock) {
            logger.warn("Invalid behind-block material '{}'", materialName)
            return
        }

        val directional = sign.block.blockData as? Directional ?: return
        val behindBlock = sign.block.getRelative(directional.facing.oppositeFace)
        if (behindBlock.type != material) {
            behindBlock.type = material
        }
    }

    companion object {
        private const val LINE_COUNT = 4
    }
}
