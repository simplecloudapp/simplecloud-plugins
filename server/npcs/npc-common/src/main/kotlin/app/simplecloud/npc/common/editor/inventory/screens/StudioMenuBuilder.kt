package app.simplecloud.npc.common.editor.inventory.screens

import app.simplecloud.npc.common.editor.Prompt
import app.simplecloud.npc.common.editor.PromptFormat
import app.simplecloud.npc.common.editor.PromptPlaceholder
import app.simplecloud.npc.common.editor.PromptResult
import app.simplecloud.npc.common.editor.PromptTokens
import app.simplecloud.npc.common.editor.core.back
import app.simplecloud.npc.common.editor.core.toggle
import app.simplecloud.npc.common.editor.inventory.CanvasMode
import app.simplecloud.npc.common.editor.inventory.ClickSummary
import app.simplecloud.npc.common.editor.inventory.InventoryEditorContext
import app.simplecloud.npc.common.editor.inventory.InventoryEditorScreen
import app.simplecloud.npc.common.editor.inventory.InventoryPickerPurpose
import app.simplecloud.npc.common.editor.inventory.LiveGroups
import app.simplecloud.npc.common.editor.inventory.StudioTab
import app.simplecloud.npc.common.editor.inventory.StudioTargets
import app.simplecloud.npc.common.editor.inventory.StudioTargets.Shared
import app.simplecloud.npc.common.editor.menu.EditorMenu
import app.simplecloud.npc.common.editor.menu.MenuClick
import app.simplecloud.npc.common.editor.menu.Pane
import app.simplecloud.npc.common.editor.ui.Clicks
import app.simplecloud.npc.common.editor.ui.Layout
import app.simplecloud.npc.common.editor.ui.Palette
import app.simplecloud.npc.common.editor.ui.Stepper
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.inventory.ItemClickActions
import app.simplecloud.npc.common.inventory.view.HeadOwners
import app.simplecloud.npc.common.inventory.view.ItemIconResolver
import app.simplecloud.npc.common.item.NpcItem
import app.simplecloud.npc.common.text.Msg
import app.simplecloud.npc.common.utils.SkinInput
import app.simplecloud.npc.core.interaction.PlayerInteraction
import app.simplecloud.npc.core.inventory.InventoryConfiguration
import app.simplecloud.npc.core.inventory.InventoryConfiguration.HeadConfiguration
import app.simplecloud.npc.core.inventory.InventoryConfiguration.InventoryItemConfiguration
import app.simplecloud.npc.core.platform.NpcPlayer
import net.kyori.adventure.text.Component

object StudioMenuBuilder {
    private const val SIZE = Layout.SIZE

    private const val LOOK_TAB = 2
    private const val CLICK_TAB = 6
    private const val PREVIEW_SLOT = Layout.HEADER

    private const val MATERIAL_SLOT = 19
    private const val NAME_SLOT = 21
    private const val LORE_SLOT = 23
    private const val AMOUNT_SLOT = 25
    private const val GLINT_SLOT = 28
    private const val TOOLTIP_SLOT = 30
    private const val MODEL_SLOT = 32
    private const val HEAD_SLOT = 34

    private val CLICK_SLOTS = mapOf(
        PlayerInteraction.LEFT_CLICK to 19,
        PlayerInteraction.RIGHT_CLICK to 21,
        PlayerInteraction.SHIFT_LEFT_CLICK to 23,
        PlayerInteraction.SHIFT_RIGHT_CLICK to 25,
    )
    private const val CLICK_INFO_SLOT = 31

    private const val GROUP_SLOT = 39
    private const val PLACEHOLDERS_SLOT = 41

    private const val SCOPE_SLOT = Layout.TOOL_LEFT
    private const val DELETE_SLOT = Layout.TOOL_RIGHT
    private const val MAX_MODEL_DATA = 9_999_999
    private val SAMPLE = mapOf(
        "<entry_name>" to "Lobby-1",
        "<entry_group_name>" to "Lobby",
        "<entry_numerical_id>" to "1",
        "<entry_online_players>" to "12",
        "<entry_max_players>" to "20",
        "<entry_state>" to "Available",
        "<entry_motd>" to "A SimpleCloud server",
        "<entry_server>" to "Lobby-1",
    )

    private val AMOUNT = Stepper(
        label = "Amount",
        range = 1.0..64.0,
        step = 1.0,
        default = 1.0,
        decimals = 0,
        material = "SLIME_BALL",
        bigMultiplier = 8,
    )

    fun build(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        screen: InventoryEditorScreen.Studio,
    ): EditorMenu {
        val session = context.session(player)
        val targets = StudioTargets.of(config, screen.slots, session.thisSlotOnly)
        val items = StudioTargets.items(config, targets)
        val first = config.items.firstOrNull { it.slot == screen.slots.first() } ?: items.firstOrNull()
        val pane = Pane(SIZE)
        if (first == null) {
            pane.back(context, player)
            return pane.menu("Item Studio")
        }
        val tab = screen.tab

        fun edit(transform: (InventoryItemConfiguration) -> InventoryItemConfiguration) {
            context.change(player, config.id) { StudioTargets.edit(it, targets, transform) }
            context.render(player, screen)
        }

        fun tabItem(slot: Int, which: StudioTab, material: String, label: String, description: String) {
            val selected = tab == which
            val name = if (selected) "<val>$label" else "<ttl>$label"
            val lore = listOf("<bd>$description") + if (selected) emptyList() else listOf("<key>Left-click to open")
            pane.left(slot, Ui.item(material, name, lore, glowing = selected)) {
                if (!selected) context.replace(player, screen.copy(tab = which))
            }
        }

        tabItem(LOOK_TAB, StudioTab.LOOK, "PAINTING", "Item Look", "How the item looks")
        tabItem(CLICK_TAB, StudioTab.CLICK, "LEVER", "Item Clicks", "What a click does")
        val livePreview = if (session.mode == CanvasMode.LIVE) session.live?.slots?.get(first.slot) else null
        val shown = livePreview ?: preview(first)
        val group = items.firstNotNullOfOrNull { it.liveGroup }
        val editing = when {
            targets.size == 1 -> "<bd>Editing: <val>This item"
            group == null -> "<bd>Editing: <val>${targets.size} items"
            else -> "<bd>Editing: <val>${targets.size} slots <bd>of <val>$group"
        }
        val intro = listOf("<hnt>Preview with sample values:", "").map(Palette::expand)
        val previewLore = listOfNotNull(
            "",
            editing,
            if (targets.size > 1) "<hnt>Fields that differ show as Mixed." else null,
        )
        pane[PREVIEW_SLOT] = shown.copy(lore = intro + shown.lore + previewLore.map(Palette::expand))

        if (group != null) {
            val only = session.thisSlotOnly
            pane.left(
                SCOPE_SLOT,
                Ui.item(
                    "CHEST_MINECART",
                    if (only) "<ttl>Editing This Slot" else "<ttl>Editing the Whole Group",
                    listOf(
                        if (only) "<bd>Changes only touch this slot." else "<bd>Changes touch every slot of <val>$group<bd>.",
                        "",
                        if (only) "<key>Left-click to edit the whole group" else "<key>Left-click to edit only this slot",
                    ),
                ),
            ) {
                session.thisSlotOnly = !only
                context.render(player, screen)
            }
        }

        when (tab) {
            StudioTab.LOOK -> {
                look(context, player, config, screen, items, pane, ::edit)
                live(context, player, config, items, pane)
            }

            StudioTab.CLICK -> click(context, player, config, items, targets, pane)
        }

        val many = targets.size > 1
        val deleted = if (many) "these ${targets.size} items" else "this item"
        pane.left(
            DELETE_SLOT,
            Ui.item(
                "BARRIER",
                "<err>Delete",
                listOf(
                    "<bd>Removes $deleted.",
                    if (many) "<hnt>Undo brings them back." else "<hnt>Undo brings it back.",
                    "",
                    if (many) "<key>Left-click to delete them" else "<key>Left-click to delete it",
                ),
            ),
        ) {
            context.change(player, config.id) { cfg ->
                cfg.copy(items = cfg.items.filterNot { it.slot in targets }.toMutableList())
            }
            context.session(player).selection.removeAll(targets)
            context.back(player)
        }
        pane.back(context, player)

        return pane.menu(Ui.title(if (tab == StudioTab.LOOK) "Item Look" else "Item Clicks", config.id))
    }

    private fun preview(item: InventoryItemConfiguration): NpcItem {
        val name = item.name
        val sampled = item.copy(name = if (name == null) null else sample(name), lore = item.lore.map(::sample).toMutableList())
        val icon = ItemIconResolver.icon(sampled)
        return icon.copy(name = icon.name.ifBlank { Ui.pretty(item.material) }, hideTooltip = false)
    }

    private fun look(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        screen: InventoryEditorScreen.Studio,
        items: List<InventoryItemConfiguration>,
        pane: Pane,
        edit: ((InventoryItemConfiguration) -> InventoryItemConfiguration) -> Unit,
    ) {
        val slots = items.map { it.slot }
        val material = StudioTargets.shared(items) { it.material }
        pane.left(
            MATERIAL_SLOT,
            Ui.item(
                (material as? Shared.Same)?.value ?: "BUNDLE",
                "<ttl>Material",
                listOf(valueLine(material) { Ui.pretty(it) }, "", "<key>Left-click to pick another"),
            ),
        ) { context.navigate(player, InventoryEditorScreen.Picker(config.id, InventoryPickerPurpose.Material(slots))) }

        val name = StudioTargets.shared(items) { it.name }
        pane.on(
            NAME_SLOT,
            Ui.item(
                "NAME_TAG",
                "<ttl>Name",
                listOf(
                    when (name) {
                        Shared.Mixed -> "<warn>Mixed"
                        is Shared.Same -> name.value?.let { "<bd>Now: $it" } ?: "<off>Default item name"
                    },
                    "<hnt>MiniMessage and <entry_*> placeholders work.",
                    "",
                    "<key>Left-click to type it in chat",
                    "<key>Press Q to clear it",
                ),
            ),
        ) { click ->
            when (click) {
                MenuClick.LEFT -> prompt(
                    context,
                    player,
                    screen,
                    "Item Name",
                    "Type the item name in chat.",
                    (name as? Shared.Same)?.value,
                    placeholders = PromptTokens.MENU +
                        PromptTokens.ENTRY.takeIf { items.any { it.liveGroup != null } }.orEmpty(),
                ) { text ->
                    context.change(player, config.id) {
                        StudioTargets.edit(it, slots.toSet()) { item -> item.copy(name = text) }
                    }
                }

                MenuClick.DROP -> edit { it.copy(name = null) }

                else -> Unit
            }
        }

        val lore = StudioTargets.shared(items) { it.lore.toList() }
        pane.left(
            LORE_SLOT,
            Ui.item(
                "WRITABLE_BOOK",
                "<ttl>Lore",
                when (lore) {
                    Shared.Mixed -> listOf("<warn>mixed")

                    is Shared.Same ->
                        lore.value
                            .ifEmpty { listOf("<off>No lore") }
                            .take(6)
                            .map { "<bd>" + it.ifBlank { " " } }
                } + listOf("", "<key>Left-click to edit the lines"),
            ),
        ) { context.navigate(player, InventoryEditorScreen.Lore(config.id, slots)) }

        val sharedAmount = StudioTargets.shared(items) { it.amount }
        val amount = (sharedAmount as? Shared.Same)?.value ?: 1
        pane[AMOUNT_SLOT] = AMOUNT.element(amount.toDouble(), mixed = sharedAmount is Shared.Mixed) { value ->
            edit { it.copy(amount = value.toInt()) }
        }

        val glint = (StudioTargets.shared(items) { it.glowing } as? Shared.Same)?.value
        pane.left(GLINT_SLOT, mixedToggle("Enchant Glint", glint, "EXPERIENCE_BOTTLE", emptyList())) {
            edit { it.copy(glowing = glint != true) }
        }

        val hidden = (StudioTargets.shared(items) { it.hideTooltip } as? Shared.Same)?.value
        val hideHint = listOf("<hnt>For decoration; hovering shows nothing.")
        pane.left(TOOLTIP_SLOT, mixedToggle("Hide Tooltip", hidden, "GLASS", hideHint)) {
            edit { it.copy(hideTooltip = hidden != true) }
        }

        val model = StudioTargets.shared(items) { it.customModelData }
        val modelLine = when (model) {
            Shared.Mixed -> "<warn>Mixed"
            is Shared.Same -> if (model.value == null) null else "<val>${model.value}"
        }
        pane.on(MODEL_SLOT, modelItem(modelLine)) { click ->
            when (click) {
                MenuClick.LEFT -> prompt(
                    context,
                    player,
                    screen,
                    "Custom Model Data",
                    "Type a number from 1 to $MAX_MODEL_DATA.",
                    (model as? Shared.Same)?.value?.toString(),
                    format = PromptFormat.PLAIN,
                    validate = { text ->
                        val number = text.toIntOrNull()
                        if (number == null || number !in 1..MAX_MODEL_DATA) {
                            PromptResult.Rejected(Component.text("Type a whole number from 1 to $MAX_MODEL_DATA."))
                        } else {
                            null
                        }
                    },
                ) { text -> edit { it.copy(customModelData = text.toInt()) } }

                MenuClick.DROP -> if (modelLine != null) edit { it.copy(customModelData = null) }

                else -> Unit
            }
        }

        val head = StudioTargets.shared(items) { it.head }
        val isHead = items.all { it.material == "PLAYER_HEAD" }
        if (!isHead) {
            pane[HEAD_SLOT] = Ui.disabled(
                "Head Texture",
                listOf("<hnt>Set the material to Player Head first."),
                "PLAYER_HEAD",
            )
        } else {
            pane.on(
                HEAD_SLOT,
                Ui.item(
                    "PLAYER_HEAD",
                    "<ttl>Head Texture",
                    listOf(
                        when (head) {
                            Shared.Mixed -> "<warn>mixed"
                            is Shared.Same -> headLine(head.value)
                        },
                        "<hnt>A texture value (e.g. from minecraft-heads.com),",
                        "<hnt>a username or UUID, or whoever opens the menu.",
                        "",
                        "<key>Left-click to type it in chat",
                        "<key>Right-click to use the viewer's head",
                        "<key>Press Q to clear it",
                    ),
                ),
            ) { click ->
                when (click) {
                    MenuClick.LEFT -> prompt(
                        context,
                        player,
                        screen,
                        "Player Head",
                        "Paste a texture value, or type a username or UUID.",
                        (head as? Shared.Same)?.value?.owner,
                        format = PromptFormat.PLAIN,
                        validate = {
                            if (headOf(it) != null) {
                                null
                            } else {
                                PromptResult.Rejected(Component.text("That is no texture, username or UUID."))
                            }
                        },
                    ) { text ->
                        val parsed = headOf(text) ?: return@prompt
                        parsed.owner?.let(HeadOwners::skin)
                        context.change(player, config.id) {
                            StudioTargets.edit(it, slots.toSet()) { item -> item.copy(head = parsed) }
                        }
                    }

                    MenuClick.RIGHT -> {
                        val viewer = (head as? Shared.Same)?.value?.showsViewer == true
                        edit { it.copy(head = if (viewer) null else HeadConfiguration(viewer = true)) }
                    }

                    MenuClick.DROP -> edit { it.copy(head = null) }

                    else -> Unit
                }
            }
        }
    }

    private fun headLine(head: HeadConfiguration?): String = when {
        head == null -> "<off>Plain head"
        head.showsViewer -> "<on>The viewer's own head"
        head.owner != null -> "<bd>Head of <val>${head.owner}"
        else -> "<bd>Custom texture set"
    }

    private fun headOf(input: String): HeadConfiguration? = when (val parsed = SkinInput.parse(input)) {
        is SkinInput.Uuid -> HeadConfiguration(owner = parsed.uuid.toString())
        is SkinInput.Username -> HeadConfiguration(owner = parsed.name)
        is SkinInput.Texture -> HeadConfiguration(texture = parsed.value, signature = parsed.signature)
        null -> null
    }

    private fun click(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        items: List<InventoryItemConfiguration>,
        targets: Set<Int>,
        pane: Pane,
    ) {
        val slots = targets.sorted()
        CLICK_SLOTS.forEach { (interaction, slot) ->
            pane.left(
                slot,
                Ui.item(
                    Clicks.material(interaction),
                    "<ttl>${Clicks.label(interaction)}",
                    ClickSummary.lines(config, items, interaction) + listOf("", "<key>Left-click to edit it"),
                    glowing = ClickSummary.hasOwn(items, interaction),
                ),
            ) { context.navigate(player, InventoryEditorScreen.ItemAction(config.id, slots, interaction)) }
        }

        pane[CLICK_INFO_SLOT] = Ui.item(
            "BOOK",
            "<ttl>How Clicks Work",
            listOf(
                "<bd>A click with nothing of its own",
                "<bd>does what the plain click does.",
            ),
        )
    }

    private fun live(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        items: List<InventoryItemConfiguration>,
        pane: Pane,
    ) {
        val group = items.firstNotNullOfOrNull { it.liveGroup } ?: return
        val sourceLabel = LiveGroups.source(config, group)?.let { LiveGroups.describe(it.type).label } ?: "none"
        pane.left(
            GROUP_SLOT,
            Ui.item(
                "OBSERVER",
                "<ttl>Live Group",
                listOf(
                    "<bd>Name: <val>$group",
                    "<bd>Source: <val>$sourceLabel",
                    "<bd>Slots: <val>${LiveGroups.contentSlots(config, group).size}",
                    "",
                    "<key>Left-click to open the group settings",
                ),
            ),
        ) { context.navigate(player, InventoryEditorScreen.LiveGroup(config.id, group)) }

        pane[PLACEHOLDERS_SLOT] = Ui.item(
            "BOOK",
            "<ttl>Placeholders",
            PromptTokens.ENTRY.map { "<val>${it.token} <hnt>${it.description}" },
        )
    }

    private fun sample(text: String): String = SAMPLE.entries.fold(text) { acc, (token, value) -> acc.replace(token, value) }

    private fun modelItem(valueLine: String?): NpcItem = Ui.item(
        "ITEM_FRAME",
        "<ttl>Custom Model Data",
        listOfNotNull(
            valueLine ?: "<off>Not set",
            "<hnt>For resource packs.",
            "",
            "<key>Left-click to type a number",
            if (valueLine == null) null else "<key>Press Q to clear it",
        ),
    )

    private fun valueLine(shared: Shared<String>, format: (String) -> String): String = when (shared) {
        Shared.Mixed -> "<warn>Mixed"
        is Shared.Same -> "<bd>Now: <val>${format(shared.value)}"
    }

    private fun prompt(
        context: InventoryEditorContext,
        player: NpcPlayer,
        screen: InventoryEditorScreen.Studio,
        title: String,
        instruction: String,
        current: String?,
        format: PromptFormat = PromptFormat.MINI_MESSAGE,
        placeholders: List<PromptPlaceholder> = emptyList(),
        validate: (String) -> PromptResult.Rejected? = PromptResult::rejectInvalidMiniMessage,
        apply: (String) -> Unit,
    ) {
        context.chatPrompt(
            player,
            Prompt(
                title = title,
                instruction = instruction,
                current = current,
                format = format,
                placeholders = placeholders,
                onCancel = { context.render(player, screen) },
                onSubmit = { input ->
                    val text = input.trim()
                    validate(text)?.let { return@Prompt it }
                    apply(text)
                    context.render(player, screen)
                    PromptResult.Accepted
                },
            ),
        )
    }

    private fun mixedToggle(label: String, value: Boolean?, material: String, hints: List<String>) = when (value) {
        null -> Ui.item(
            material,
            "<ttl>$label",
            listOf("<warn>Mixed") + hints + listOf("", "<key>Left-click to turn it on for all"),
        )

        else -> Ui.toggle(label, value, hints, material)
    }
}
