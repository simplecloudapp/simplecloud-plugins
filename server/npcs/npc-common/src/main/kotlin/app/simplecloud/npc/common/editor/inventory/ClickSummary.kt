package app.simplecloud.npc.common.editor.inventory

import app.simplecloud.npc.common.editor.ui.Clicks
import app.simplecloud.npc.common.inventory.ItemClickActions
import app.simplecloud.npc.core.interaction.PlayerInteraction
import app.simplecloud.npc.core.inventory.InventoryConfiguration
import app.simplecloud.npc.core.inventory.InventoryConfiguration.InventoryItemConfiguration

/** What a click on menu items really does, including the fallbacks that run when the click has no own actions. */
object ClickSummary {

    fun lines(config: InventoryConfiguration, items: List<InventoryItemConfiguration>, click: PlayerInteraction): List<String> {
        val each = items.map { lines(config, it, click) }.distinct()
        return each.singleOrNull() ?: listOf("<warn>Mixed", "<hnt>The selected items differ.")
    }

    fun hasOwn(items: List<InventoryItemConfiguration>, click: PlayerInteraction): Boolean =
        items.any { ItemClickActions.of(it, click)?.configuredTypes().orEmpty().isNotEmpty() }

    private fun lines(config: InventoryConfiguration, item: InventoryItemConfiguration, click: PlayerInteraction): List<String> {
        val own = ItemClickActions.of(item, click)?.configuredTypes().orEmpty()
        if (own.isNotEmpty()) return own.map { "<bd>· <val>$it" }

        val group = item.liveGroup
        val source = if (group == null) null else LiveGroups.source(config, group)
        if (source != null && item.actions.isEmpty()) {
            return listOf("<bd>Runs what <val>${LiveGroups.describe(source.type).label}", "<bd>entries do by default.")
        }

        val fallback = ItemClickActions.forClick(item.actions, click).firstOrNull()?.interactionType
        if (fallback != null && fallback != click) return listOf("<bd>Does what <val>${Clicks.label(fallback)} <bd>does.")

        return listOf("<off>Nothing")
    }
}
