package app.simplecloud.plugin.command.shared.utilities

import app.simplecloud.plugin.command.shared.config.FormatSection
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

class Formatter(private val format: FormatSection) {

    private val logger = LoggerFactory.getLogger(Formatter::class.java)

    private val dateFormatter = createDateFormatter()

    val empty: String = format.empty

    fun parseTime(value: String?): Instant? {
        if (value.isNullOrBlank()) return null
        return runCatching { Instant.parse(value) }.getOrNull()
    }

    fun date(value: String?): String {
        val instant = parseTime(value) ?: return empty
        return dateFormatter.format(instant)
    }

    fun since(value: String?): String {
        val instant = parseTime(value) ?: return empty
        return Duration.between(instant, Instant.now()).toSeconds().coerceAtLeast(0).seconds.toString()
    }

    fun memory(min: Int?, max: Int?): String {
        if (min == null && max == null) return empty
        return "${min ?: empty}-${max ?: empty} MB"
    }

    fun yesNo(value: Boolean?): String = when (value) {
        true -> "Yes"
        false -> "No"
        null -> empty
    }

    private fun createDateFormatter(): DateTimeFormatter {
        val defaults = FormatSection()
        val pattern = runCatching { DateTimeFormatter.ofPattern(format.date, Locale.getDefault()) }.getOrElse {
            logger.warn("Invalid date format '${format.date}' in messages.yml, using '${defaults.date}'", it)
            DateTimeFormatter.ofPattern(defaults.date, Locale.getDefault())
        }
        return pattern.withZone(createZone())
    }

    private fun createZone(): ZoneId {
        if (format.zone == "system") return ZoneId.systemDefault()
        return runCatching { ZoneId.of(format.zone) }.getOrElse {
            logger.warn("Invalid time zone '${format.zone}' in messages.yml, using the system time zone", it)
            ZoneId.systemDefault()
        }
    }
}
