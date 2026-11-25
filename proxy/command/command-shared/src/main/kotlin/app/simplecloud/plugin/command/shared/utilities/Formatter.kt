package app.simplecloud.plugin.command.shared.utilities

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

object Formatter {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault())

    fun parseTime(value: String?): Instant? {
        if (value.isNullOrBlank()) return null
        return runCatching { Instant.parse(value) }.getOrNull()
    }

    fun date(value: String?): String {
        val instant = parseTime(value) ?: return "-"
        return dateFormatter.format(instant)
    }

    fun since(value: String?): String {
        val instant = parseTime(value) ?: return "-"
        return Duration.between(instant, Instant.now()).toSeconds().seconds.toString()
    }

    fun yesNo(value: Boolean?): String = when (value) {
        true -> "Yes"
        false -> "No"
        null -> "-"
    }
}
