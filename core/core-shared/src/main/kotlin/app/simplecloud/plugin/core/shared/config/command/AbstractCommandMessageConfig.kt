package app.simplecloud.plugin.core.shared.config.command

import app.simplecloud.plugin.core.shared.config.message.AbstractMessageConfig

abstract class AbstractCommandMessageConfig(
    palette: Map<String, String>,
    variables: Map<String, String>,
) : AbstractMessageConfig(palette, variables) {

    abstract val command: AbstractCommandMessages
}
