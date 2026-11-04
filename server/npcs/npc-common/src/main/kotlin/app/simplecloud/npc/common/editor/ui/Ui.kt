package app.simplecloud.npc.common.editor.ui

import app.simplecloud.npc.common.item.NpcItem
import java.util.UUID

object Ui {
    private const val ADD_HEAD_TEXTURE =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBiNTVmNzQ2ODFjNjgyODNhMWMxY2U1MWYxYzgzYjUyZTI5NzFjOTFlZTM0ZWZjYjU5OGRmMzk5MGE3ZTcifX19"

    private const val TITLE_NAME_LENGTH = 18
    private const val DISABLED_MATERIAL = "LIGHT_GRAY_DYE"
    private const val BACK_MATERIAL = "RED_STAINED_GLASS_PANE"
    private const val PAGE_MATERIAL = "BLUE_STAINED_GLASS_PANE"

    fun item(
        material: String,
        name: String,
        lore: List<String> = emptyList(),
        glowing: Boolean = false,
        amount: Int = 1,
    ): NpcItem = NpcItem(
        material,
        Palette.expand(name),
        tidy(lore).map(Palette::expand),
        amount = amount,
        glowing = glowing,
    )

    fun head(
        name: String,
        lore: List<String> = emptyList(),
        texture: String? = null,
        signature: String? = null,
        owner: UUID? = null,
        glowing: Boolean = false,
    ): NpcItem = NpcItem.playerHead(
        name = Palette.expand(name),
        lore = tidy(lore).map(Palette::expand),
        owner = owner,
        texture = texture,
        signature = signature,
        glowing = glowing,
    )

    fun option(
        material: String,
        name: String,
        lore: List<String>,
        selected: Boolean,
        nameColor: String? = null,
    ): NpcItem = item(
        material,
        when {
            nameColor != null -> "$nameColor$name"
            selected -> "<val>$name"
            else -> "<ttl>$name"
        },
        lore + "" + if (selected) "<on>Selected" else "<key>Left-click to select",
        glowing = selected,
    )

    fun disabled(name: String, lore: List<String>): NpcItem = item(DISABLED_MATERIAL, "<off>$name", lore)

    fun unavailable(name: String, lore: List<String>, reason: String): NpcItem =
        disabled(name, lore + listOf("", "<hnt>$reason"))

    fun field(material: String, label: String, valueLine: String?, setVerb: String): NpcItem = item(
        material,
        "<ttl>$label",
        listOfNotNull(
            valueLine ?: "<off>Not set",
            "",
            setVerb,
            "<key>Press Q to clear it".takeIf { valueLine != null },
        ),
        glowing = valueLine != null,
    )

    fun toggle(
        label: String,
        enabled: Boolean,
        extraLore: List<String> = emptyList(),
        material: String? = null,
    ): NpcItem = item(
        material ?: if (enabled) "LIME_DYE" else "GRAY_DYE",
        "<ttl>$label",
        listOf(if (enabled) "<on>On" else "<off>Off") + extraLore +
            listOf("", if (enabled) "<key>Left-click to turn it off" else "<key>Left-click to turn it on"),
        glowing = enabled,
    )

    fun addHead(label: String, lore: List<String> = emptyList()): NpcItem =
        head("<ttl>$label", lore, texture = ADD_HEAD_TEXTURE)

    fun back(to: String? = null): NpcItem = item(
        BACK_MATERIAL,
        "<key>Back",
        listOfNotNull(if (to == null) null else "<bd>To <val>${to.substringAfterLast(" · ")}", "<hnt>Esc does the same."),
    )

    fun previousPage(page: Int): NpcItem? =
        if (page <= 0) null else item(PAGE_MATERIAL, "<key>Previous Page", listOf("<bd>Page <val>$page"))

    fun nextPage(page: Int, pageCount: Int): NpcItem? =
        if (page >= pageCount - 1) null else item(PAGE_MATERIAL, "<key>Next Page", listOf("<bd>Page <val>${page + 2}"))

    fun pageIndicator(page: Int, pageCount: Int, lore: List<String>): NpcItem =
        item("PAPER", "<ttl>Page ${page + 1}/$pageCount", lore)

    /** The title of an editor's first screen, e.g. `Edit limbo`. */
    fun root(name: String): String = "Edit ${clipName(name)}"

    /** The title of a screen below the first one, e.g. `Edit limbo · Skin`. */
    fun title(section: String, npcName: String): String = "Edit ${clipName(npcName)} · $section"

    private fun clipName(name: String): String =
        if (name.length <= TITLE_NAME_LENGTH) name else name.take(TITLE_NAME_LENGTH) + "…"

    fun pretty(material: String): String =
        material.lowercase().split('_').joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

    fun quote(text: String): String = "\"$text\""

    /** Drops empty lines at the start and the end and collapses repeated ones, so tooltips never carry padding. */
    private fun tidy(lore: List<String>): List<String> {
        val lines = lore.dropWhile { it.isBlank() }.dropLastWhile { it.isBlank() }
        return lines.filterIndexed { index, line -> line.isNotBlank() || lines[index - 1].isNotBlank() }
    }
}

internal fun <T> List<T>.after(current: T): T = this[(indexOf(current) + 1) % size]
