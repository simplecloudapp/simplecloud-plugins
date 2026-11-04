package app.simplecloud.npc.common.editor.npc.picker

import app.simplecloud.npc.common.editor.core.EmptyState
import app.simplecloud.npc.common.editor.core.PickerHeader
import app.simplecloud.npc.common.editor.core.PickerOption
import app.simplecloud.npc.common.editor.core.PickerOptions
import app.simplecloud.npc.common.editor.npc.NpcEditorContext
import app.simplecloud.npc.common.editor.npc.NpcFormat
import app.simplecloud.npc.common.editor.npc.PickerPurpose
import app.simplecloud.npc.common.editor.ui.Clicks
import app.simplecloud.npc.core.config.NpcConfig
import app.simplecloud.npc.core.platform.NpcPlayer

class PickMenuSource(override val purpose: PickerPurpose.PickMenu) : PickerSource {
    override val title = "Pick A Menu"

    override fun header(context: NpcEditorContext, config: NpcConfig) = PickerHeader(
        "CHEST",
        "Which Menu Opens?",
        listOf(
            "<bd>Opens when a player ${Clicks.verb(purpose.interaction)}",
            "<val>${NpcFormat.displayName(config)}<bd>.",
            "",
            PickerHeader.now(config.findAction(purpose.interaction, purpose.joinState)?.openInventory),
        ),
    )

    override val empty = EmptyState(
        "No Menus Yet",
        listOf("<bd>Create one with", "<val>/scnpcs inventory create"),
    )
    override val typeHint: List<String>? = null
    override val typingUnavailable = "Menus are created in the Menu Builder."

    override fun options(context: NpcEditorContext, player: NpcPlayer, config: NpcConfig): List<PickerOption> =
        PickerOptions.menus(
            context.inventoryRepository.findAll(),
            config.findAction(purpose.interaction, purpose.joinState)?.openInventory,
        )

    override fun apply(context: NpcEditorContext, player: NpcPlayer, config: NpcConfig, value: String) {
        context.commitAndBack(player, config.id) { fresh ->
            fresh.apply { actionOrCreate(purpose.interaction, purpose.joinState).openInventory = value }
        }
    }
}
