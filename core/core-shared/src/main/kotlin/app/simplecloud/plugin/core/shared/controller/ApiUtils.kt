package app.simplecloud.plugin.core.shared.controller

import app.simplecloud.api.CloudApi
import app.simplecloud.api.CloudApiException
import app.simplecloud.api.group.Group
import app.simplecloud.api.persistentserver.PersistentServer
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeoutException

object ApiUtils {

    private val logger = LoggerFactory.getLogger(ApiUtils::class.java)
    private val statusCodeSuffix = Regex("""\s*\(\d+\)$""")
    private val apiAvailable = isApiAvailable()

    suspend fun findGroup(api: CloudApi, name: String): Group? =
        api.group().findGroupByName(name).await().orElse(null)
            ?: api.group().allGroups.await().firstOrNull { it.name.equals(name, true) }

    suspend fun findPersistentServer(api: CloudApi, name: String): PersistentServer? =
        api.persistentServer().findPersistentServerByName(name).await().orElse(null)
            ?: api.persistentServer().allPersistentServers.await().firstOrNull { it.name.equals(name, true) }

    fun reason(throwable: Throwable): String {
        val causes = generateSequence(throwable) { it.cause }
        if (!apiAvailable) return nonApiReason(throwable, causes)

        val exception = causes.filterIsInstance<CloudApiException>().firstOrNull()

        return when {
            exception == null -> nonApiReason(throwable, causes)
            exception.reason == CloudApiException.Reason.UNAVAILABLE && exception.statusCode <= 0 -> "the controller is not reachable"
            exception.reason == CloudApiException.Reason.UNAVAILABLE -> "the controller is currently unavailable"
            else -> formatMessage(exception.message)
        }
    }

    private fun isApiAvailable(): Boolean = try {
        Class.forName("app.simplecloud.api.CloudApiException", false, ApiUtils::class.java.classLoader)
        true
    } catch (_: ClassNotFoundException) {
        false
    }

    private fun nonApiReason(throwable: Throwable, causes: Sequence<Throwable>): String {
        if (causes.any { it is TimeoutException }) return "the controller did not respond in time"
        logger.error("An unexpected error occurred while executing a command", throwable)
        return "an internal error occurred"
    }

    private fun formatMessage(error: String?): String {
        val message = error.orEmpty().replace(statusCodeSuffix, "").trim().trimEnd('.')
        if (message.isBlank()) return "an unknown error occurred"
        val isAcronym = message.length > 1 && message[1].isUpperCase()
        return if (isAcronym) message else message.replaceFirstChar { it.lowercase() }
    }
}
