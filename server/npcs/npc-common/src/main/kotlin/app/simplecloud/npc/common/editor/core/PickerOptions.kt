package app.simplecloud.npc.common.editor.core

import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.text.Msg
import app.simplecloud.npc.core.inventory.InventoryConfiguration

object PickerOptions {

    val SOUND_TYPE_HINT = listOf(
        "<bd>Sound keys are a lot to page through.",
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

    fun sounds(catalog: List<String>, filter: String?, current: String?): List<PickerOption> = catalog
        .filter { filter == null || it.contains(filter, ignoreCase = true) }
        .map { key ->
            val selected = key.equals(current, true)
            PickerOption(
                key,
                Ui.choice("NOTE_BLOCK", key, listOf("<key>Right-click to hear it"), selected, "use this sound"),
                available = !selected,
            )
        }

    fun servers(servers: List<LiveServer>, current: String?): List<PickerOption> = servers.map { server ->
        val selected = server.name.equals(current, true)
        when {
            server.status == ServerStatus.OFFLINE -> PickerOption(
                server.name,
                Ui.unavailable(server.name, emptyList(), "Offline, so nobody can join it."),
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
                    "send players here",
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
                    "open this menu",
                ),
                available = !selected,
            )
        }
}
