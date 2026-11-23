package app.simplecloud.plugin.connection.shared.connection

sealed interface ConnectionTarget {

    data class Connect(val serverName: String) : ConnectionTarget

    data object AlreadyConnected : ConnectionTarget

    data object NotFound : ConnectionTarget
}
