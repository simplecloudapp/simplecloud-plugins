package app.simplecloud.npc.common.editor.core

import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.text.Msg
import app.simplecloud.npc.core.inventory.InventoryConfiguration

object PickerOptions {

    val SOUND_TYPE_HINT = listOf(
        "<bd>Looking for a certain sound?",
        "",
        "<key>Left-click to type it in chat",
        "<hnt>A fragment filters the list above;",
        "<hnt>an exact key is picked straight away.",
    )

    val MATERIAL_TYPE_HINT = listOf(
        "<bd>Search every material.",
        "",
        "<key>Left-click to type it in chat",
        "<hnt>A fragment filters the list;",
        "<hnt>an exact name is picked straight away.",
    )

    private const val MAX_AMBIGUOUS_MATCHES = 3

    fun match(candidates: List<String>, typed: String, notFound: String): PickerResult? {
        if (candidates.any { it.equals(typed, true) }) return null
        val partial = candidates.filter { it.contains(typed, ignoreCase = true) }

        return if (partial.isEmpty()) {
            PickerResult.Rejected("Not found", typed, notFound)
        } else {
            PickerResult.Ambiguous(typed, partial.take(MAX_AMBIGUOUS_MATCHES))
        }
    }

    fun search(catalog: List<String>, typed: String, noun: String): PickerResult? {
        if (catalog.any { it.equals(typed, true) }) return null
        val matching = catalog.count { it.contains(typed, ignoreCase = true) }

        return if (matching == 0) {
            PickerResult.Rejected("Not found", typed, "No $noun contains that.")
        } else {
            PickerResult.Filtered(typed, matching)
        }
    }

    fun canonical(catalog: List<String>, value: String): String =
        catalog.firstOrNull { it.equals(value, true) } ?: value

    fun statusText(status: ServerStatus): String = when (status) {
        ServerStatus.ONLINE -> "<on>Online"
        ServerStatus.OFFLINE -> "<off>Offline"
        else -> "<warn>${status.name.lowercase().replaceFirstChar(Char::uppercase)}"
    }

    private val COMMON_SOUNDS = listOf(
        "UI_BUTTON_CLICK",
        "ENTITY_EXPERIENCE_ORB_PICKUP",
        "ENTITY_PLAYER_LEVELUP",
        "BLOCK_NOTE_BLOCK_PLING",
        "BLOCK_NOTE_BLOCK_BELL",
        "BLOCK_NOTE_BLOCK_CHIME",
        "BLOCK_AMETHYST_BLOCK_CHIME",
        "ENTITY_VILLAGER_YES",
        "ENTITY_VILLAGER_NO",
        "BLOCK_CHEST_OPEN",
        "BLOCK_ENDER_CHEST_OPEN",
        "BLOCK_WOODEN_DOOR_OPEN",
        "ENTITY_ENDERMAN_TELEPORT",
        "ENTITY_ITEM_PICKUP",
        "UI_TOAST_CHALLENGE_COMPLETE",
        "BLOCK_BEACON_ACTIVATE",
        "ENTITY_FIREWORK_ROCKET_LAUNCH",
        "ENTITY_SLIME_JUMP",
        "BLOCK_ANVIL_LAND",
        "ENTITY_CHICKEN_EGG",
        "BLOCK_BELL_USE",
    )

    fun sounds(catalog: List<String>, filter: String?, current: String?): List<PickerOption> = ordered(catalog, filter)
        .map { key ->
            val selected = key.equals(current, true)
            PickerOption(
                key,
                Ui.choice("NOTE_BLOCK", soundName(key), listOf("<hnt>$key"), selected, "use this sound", listOf("<key>Right-click to hear it")),
                available = !selected,
            )
        }

    private val SOUND_GROUPS = setOf("BLOCK", "ENTITY", "UI", "ITEM", "AMBIENT", "MUSIC", "EVENT", "WEATHER")

    private fun soundName(key: String): String {
        val group = key.substringBefore('_')
        return Ui.pretty(if (group in SOUND_GROUPS && '_' in key) key.substringAfter('_') else key)
    }

    private fun ordered(catalog: List<String>, filter: String?): List<String> {
        if (filter != null) return catalog.filter { it.contains(filter, ignoreCase = true) }
        val common = COMMON_SOUNDS.filter { it in catalog }
        return common + (catalog - common.toSet())
    }

    fun servers(servers: List<LiveServer>, current: String?): List<PickerOption> = servers.map { server ->
        val selected = server.name.equals(current, true)
        when {
            server.status == ServerStatus.OFFLINE -> PickerOption(
                server.name,
                Ui.unavailable(server.name, emptyList(), "Offline, so nobody can join it.", "MAP"),
                available = false,
            )

            else -> PickerOption(
                server.name,
                Ui.choice(
                    "MAP",
                    Msg.miniMessage.escapeTags(server.name),
                    listOf(
                        "<bd>Status: ${statusText(server.status)}",
                        "<bd>Players: <val>${server.playerCount}",
                    ),
                    selected,
                    "pick this server",
                ),
                available = !selected,
            )
        }
    }

    fun menus(menus: List<InventoryConfiguration>, current: String?): List<PickerOption> = menus
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.id })
        .map { menu ->
            val selected = menu.id.equals(current, true)
            PickerOption(
                menu.id,
                Ui.choice(
                    "CHEST",
                    Msg.miniMessage.escapeTags(menu.id),
                    listOf("<bd>Rows: <val>${menu.rows}", "<bd>Items: <val>${menu.items.size}"),
                    selected,
                    "pick this menu",
                ),
                available = !selected,
            )
        }
}
