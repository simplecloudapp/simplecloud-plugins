package app.simplecloud.plugin.core.shared.config

abstract class AbstractCommandMessageConfig : AbstractMessageConfig() {

    abstract val command: AbstractCommandMessages
}
