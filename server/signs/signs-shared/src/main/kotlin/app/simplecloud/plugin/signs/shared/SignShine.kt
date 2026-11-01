package app.simplecloud.plugin.signs.shared

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

object SignShine {

    private const val WIDTH = 2.5
    private const val SWEEP_SHARE = 0.6
    private const val DEFAULT_DURATION = 2000L

    val resolver: TagResolver = TagResolver.resolver("shine") { arguments, context ->
        val text = arguments.popOr("shine expects a text").value()
        val base = color(arguments.popOr("shine expects a base color").value()) ?: throw context.newException("Invalid base color", arguments)
        val highlight = color(arguments.popOr("shine expects a highlight color").value()) ?: throw context.newException("Invalid highlight color", arguments)
        val duration = if (arguments.hasNext()) arguments.pop().value().toLongOrNull() else DEFAULT_DURATION
        if (duration == null || duration <= 0) throw context.newException("Invalid duration", arguments)

        val progress = System.currentTimeMillis() % duration / duration.toDouble()
        val characters = text.codePoints().toArray()
        val component = Component.text()
        intensities(characters.size, progress).forEachIndexed { index, intensity ->
            component.append(Component.text(Character.toString(characters[index]), TextColor.lerp(intensity, base, highlight)))
        }
        Tag.selfClosingInserting(component.build())
    }

    fun intensities(length: Int, progress: Double): List<Float> {
        val sweep = (progress / SWEEP_SHARE).coerceAtMost(1.0)
        val center = -WIDTH + sweep * (length - 1 + 2 * WIDTH)
        return List(length) { index ->
            val distance = abs(index - center) / WIDTH
            if (distance >= 1) 0f else ((1 + cos(PI * distance)) / 2).toFloat()
        }
    }

    private fun color(value: String): TextColor? = TextColor.fromHexString(value)
}
