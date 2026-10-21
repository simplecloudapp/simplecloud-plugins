package app.simplecloud.plugin.connection.shared.connection

import app.simplecloud.plugin.connection.shared.platform.ConnectionServer

sealed interface CommandTarget<out S : ConnectionServer> {

    data class Connect<S : ConnectionServer>(val server: S) : CommandTarget<S>

    data object AlreadyConnected : CommandTarget<Nothing>

    data object NotFound : CommandTarget<Nothing>
}
