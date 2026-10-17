package app.simplecloud.plugin.notify.shared.platform

import app.simplecloud.plugin.notify.shared.command.CloudSender
import java.io.File

/**
 * Represents a platform on which notify runs.
 */
interface NotifyPlatform {

    /**
     * Returns this platform's data directory.
     */
    fun getDataDirectory(): File

    /**
     * Returns all players that are currently online on this proxy.
     */
    fun getOnlinePlayers(): Collection<CloudSender>
}
