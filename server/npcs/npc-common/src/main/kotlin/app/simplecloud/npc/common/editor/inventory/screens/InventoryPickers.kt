package app.simplecloud.npc.common.editor.inventory.screens

import app.simplecloud.npc.common.editor.core.EmptyState
import app.simplecloud.npc.common.editor.core.PickerHeader
import app.simplecloud.npc.common.editor.core.PickerOption
import app.simplecloud.npc.common.editor.core.PickerOptions
import app.simplecloud.npc.common.editor.core.PickerResult
import app.simplecloud.npc.common.editor.core.PickerSpec
import app.simplecloud.npc.common.editor.core.PickerTemplate
import app.simplecloud.npc.common.editor.core.SoundTuning
import app.simplecloud.npc.common.editor.core.pageKey
import app.simplecloud.npc.common.editor.inventory.Carried
import app.simplecloud.npc.common.editor.inventory.CommonMaterials
import app.simplecloud.npc.common.editor.inventory.InventoryEditorContext
import app.simplecloud.npc.common.editor.inventory.InventoryEditorScreen
import app.simplecloud.npc.common.editor.inventory.InventoryEditorSession
import app.simplecloud.npc.common.editor.inventory.InventoryPickerPurpose
import app.simplecloud.npc.common.editor.inventory.LiveGroups
import app.simplecloud.npc.common.editor.inventory.StudioTargets
import app.simplecloud.npc.common.editor.menu.EditorMenu
import app.simplecloud.npc.common.editor.menu.Pane
import app.simplecloud.npc.common.editor.ui.Clicks
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.inventory.ItemClickActions
import app.simplecloud.npc.common.inventory.source.GroupServersSource
import app.simplecloud.npc.core.config.NpcConfig.ActionConfiguration
import app.simplecloud.npc.core.interaction.PlayerInteraction
import app.simplecloud.npc.core.inventory.InventoryConfiguration
import app.simplecloud.npc.core.inventory.InventoryConfiguration.InventoryItemConfiguration
import app.simplecloud.npc.core.platform.NpcPlayer

object InventoryPickers {

    private val CATEGORY_SLOTS = listOf(1, 2, 3, 5, 6, 7)
    private val CATEGORY_ICONS = mapOf(
        "Blocks" to "GRASS_BLOCK",
        "Tools" to "IRON_PICKAXE",
        "Food" to "APPLE",
        "Dyes" to "RED_DYE",
        "Heads" to "PLAYER_HEAD",
        InventoryEditorSession.RECENT_CATEGORY to "KNOWLEDGE_BOOK",
    )

    fun build(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        purpose: InventoryPickerPurpose,
    ): EditorMenu {
        val screen = InventoryEditorScreen.Picker(config.id, purpose)
        val spec = when (purpose) {
            InventoryPickerPurpose.NewItem,
            is InventoryPickerPurpose.Material,
            is InventoryPickerPurpose.LookMaterial,
            -> materials(context, player, config, screen, purpose)

            InventoryPickerPurpose.NewLiveGroup -> sourceTypes(context, player, config, screen, null)

            is InventoryPickerPurpose.LiveSourceType -> sourceTypes(context, player, config, screen, purpose.group)

            is InventoryPickerPurpose.LiveSourceGroup -> sourceGroups(context, player, config, screen, purpose.group)

            InventoryPickerPurpose.UsedBy -> usedBy(context, player, config, screen)

            is InventoryPickerPurpose.OpenInventory -> openInventory(context, player, config, screen, purpose)

            is InventoryPickerPurpose.SendToServer -> sendToServer(context, player, config, screen, purpose)

            is InventoryPickerPurpose.Sound -> sounds(context, player, config, screen, purpose)
        }

        return PickerTemplate.build(context, player, spec)
    }

    private fun materials(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        screen: InventoryEditorScreen.Picker,
        purpose: InventoryPickerPurpose,
    ): PickerSpec<InventoryEditorScreen> {
        val session = context.session(player)
        val catalog = context.materialCatalog()
        val known = catalog.toSet()
        val filter = (session.pickerResult as? PickerResult.Filtered)?.typed
        val current = when (purpose) {
            is InventoryPickerPurpose.Material ->
                config.items.filter { it.slot in purpose.slots }.map { it.material }.distinct().singleOrNull()

            is InventoryPickerPurpose.LookMaterial ->
                LiveGroups.source(config, purpose.group)?.lookFor(purpose.state)?.material

            else -> null
        }

        val listed = when {
            filter != null -> catalog.filter { it.contains(filter, ignoreCase = true) }

            session.materialCategory == InventoryEditorSession.RECENT_CATEGORY ->
                session.recentMaterials.ifEmpty { CommonMaterials.COMMON.take(RECENT_FALLBACK) }

            else -> CommonMaterials.CATEGORIES.firstOrNull { it.first == session.materialCategory }?.second.orEmpty()
        }.filter { it in known }

        fun pick(material: String) {
            val canonical = catalog.firstOrNull { it.equals(material, true) } ?: return
            session.usedMaterial(canonical)
            session.pickerResult = null
            when (purpose) {
                is InventoryPickerPurpose.Material -> context.change(player, config.id) { fresh ->
                    StudioTargets.edit(fresh, purpose.slots.toSet()) { it.copy(material = canonical) }
                }

                is InventoryPickerPurpose.LookMaterial -> context.change(player, config.id) { fresh ->
                    LiveGroups.setLook(fresh, purpose.group, purpose.state) { it.copy(material = canonical) }
                }

                else -> session.carried = Carried(InventoryItemConfiguration(material = canonical), from = null)
            }
            context.back(player)
        }

        val section = if (purpose is InventoryPickerPurpose.NewItem) "New Item" else "Material"
        return PickerSpec(
            title = Ui.title(section, config.id),
            screen = screen,
            header = PickerHeader(
                current ?: "ITEM_FRAME",
                "Which Material?",
                materialLines(purpose, current),
            ),
            options = listed.map { material ->
                val selected = material == current
                PickerOption(
                    material,
                    Ui.choice(material, Ui.pretty(material), emptyList(), selected, "use it"),
                    available = !selected,
                )
            },
            onPick = ::pick,
            typeHint = PickerOptions.MATERIAL_TYPE_HINT,
            onTyped = { typed ->
                PickerOptions.search(catalog, typed, "material") ?: run {
                    pick(typed)
                    null
                }
            },
            extras = { pane -> categoryChips(context, player, screen, pane) },
            filteredNoun = "materials",
        )
    }

    private fun categoryChips(
        context: InventoryEditorContext,
        player: NpcPlayer,
        screen: InventoryEditorScreen.Picker,
        pane: Pane,
    ) {
        val session = context.session(player)
        val filtered = session.pickerResult is PickerResult.Filtered
        val tabs = CommonMaterials.CATEGORIES.map { it.first }
        val filters = listOf(InventoryEditorSession.RECENT_CATEGORY)
        val slots = (filters + tabs).zip(CATEGORY_SLOTS)

        slots.forEach { (name, slot) ->
            val selected = !filtered && session.materialCategory == name
            val icon = CATEGORY_ICONS[name] ?: "PAPER"
            val tab = if (selected) {
                Ui.item(icon, "<val>$name", listOf("<on>Shown below"), glowing = true)
            } else {
                Ui.item(icon, "<ttl>$name", listOf("<key>Left-click to show these"))
            }
            pane.left(slot, tab) {
                session.materialCategory = name
                session.pickerResult = null
                session.pages[pageKey(screen)] = 0
                context.render(player, screen)
            }
        }
    }

    private fun sourceTypes(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        screen: InventoryEditorScreen.Picker,
        group: String?,
    ): PickerSpec<InventoryEditorScreen> {
        val current = group?.let { LiveGroups.source(config, it)?.type }
        return PickerSpec(
            title = Ui.title(if (group == null) "New Live Group" else "Source", config.id),
            screen = screen,
            header = PickerHeader(
                "OBSERVER",
                "Where Do Entries Come From?",
                listOf(
                    "<bd>A live group fills its slots",
                    "<bd>with one entry per server or",
                    "<bd>target, kept up to date.",
                    "",
                    PickerHeader.now(if (current == null) null else LiveGroups.describe(current).label),
                ),
            ),
            options = context.dataSourceTypes().map { type ->
                val info = LiveGroups.describe(type)
                PickerOption(
                    type,
                    Ui.choice(info.icon, info.label, info.lines.map { "<bd>$it" }, type == current, "use this source"),
                    available = type != current,
                )
            },
            typeHint = null,
            typingUnavailable = "Sources come from the plugin and its addons.",
            onPick = { type ->
                if (group == null) {
                    val name = LiveGroups.freeName(config, type)
                    val session = context.session(player)
                    session.carried = Carried(LiveGroups.newSlot(name, type), from = null)
                    session.activeGroup = name
                } else {
                    context.change(player, config.id) {
                        LiveGroups.setSource(it, group) { source -> source.copy(type = type) }
                    }
                }
                context.back(player)
            },
        )
    }

    private fun sourceGroups(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        screen: InventoryEditorScreen.Picker,
        group: String,
    ): PickerSpec<InventoryEditorScreen> {
        val current = LiveGroups.source(config, group)?.group ?: GroupServersSource.TARGET_GROUP
        val targetOption = PickerOption(
            GroupServersSource.TARGET_GROUP,
            Ui.choice(
                "ARMOR_STAND",
                "The NPC's Target",
                listOf("<bd>Whatever group the NPC that", "<bd>opened the menu targets."),
                current == GroupServersSource.TARGET_GROUP,
                "use it",
            ),
            available = current != GroupServersSource.TARGET_GROUP,
        )
        val groups = context.liveData.knownTargets().filter { it.group }.map { target ->
            PickerOption(
                target.name,
                Ui.choice(
                    "BOOKSHELF",
                    target.name,
                    listOf("<bd>Servers: <val>${context.liveData.summaryOf(target.name).liveServers}"),
                    target.name.equals(current, true),
                    "use this group",
                ),
                available = !target.name.equals(current, true),
            )
        }

        return PickerSpec(
            title = Ui.title("Group", config.id),
            screen = screen,
            header = PickerHeader(
                "BOOKSHELF",
                "Which Group?",
                listOf(
                    "<bd>Every server of this group",
                    "<bd>gets an entry in <val>$group<bd>.",
                    "",
                    PickerHeader.now(
                        if (current == GroupServersSource.TARGET_GROUP) "The NPC's Target" else current,
                    ),
                ),
            ),
            options = listOf(targetOption) + groups,
            onPick = { value ->
                context.change(player, config.id) {
                    LiveGroups.setSource(it, group) { source -> source.copy(group = value) }
                }
                context.back(player)
            },
        )
    }

    private fun usedBy(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        screen: InventoryEditorScreen.Picker,
    ): PickerSpec<InventoryEditorScreen> = PickerSpec(
        title = Ui.title("Used By", config.id),
        header = PickerHeader(
            "ARMOR_STAND",
            "NPCs That Open ${config.id}",
            listOf("<bd>These NPCs have an Open Menu", "<bd>action for this menu."),
        ),
        empty = EmptyState("No NPC Opens This Menu", listOf("<bd>Give an NPC an Open Menu action", "<bd>for this menu to see it here.")),
        screen = screen,
        options = InventoryHubMenuBuilder.usedBy(context, config.id).map { npc ->
            PickerOption(
                npc.id,
                Ui.item("ARMOR_STAND", "<ttl>${npc.id}", listOf("<key>Left-click to open its editor")),
            )
        },
        typeHint = null,
        typingUnavailable = "Only NPCs that open this menu are listed.",
        onPick = { id ->
            val npc = context.npcRepository.find(id) ?: return@PickerSpec
            context.close(player)
            context.editors.openNpc(player, npc)
        },
    )

    private fun openInventory(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        screen: InventoryEditorScreen.Picker,
        purpose: InventoryPickerPurpose.OpenInventory,
    ): PickerSpec<InventoryEditorScreen> = PickerSpec(
        title = Ui.title("Pick a Menu", config.id),
        screen = screen,
        header = PickerHeader(
            "CHEST",
            "Which Menu Opens?",
            clickedLines(purpose.slots, purpose.interaction, "Opens when a player") +
                PickerHeader.now(currentAction(config, purpose.slots, purpose.interaction)?.openInventory),
        ),
        options = PickerOptions.menus(
            context.inventoryRepository.findAll(),
            currentAction(config, purpose.slots, purpose.interaction)?.openInventory,
        ),
        typeHint = null,
        typingUnavailable = "Create the menu first, then pick it here.",
        onPick = { id ->
            editClicks(context, player, config, purpose.slots, purpose.interaction) { it.openInventory = id }
            context.back(player)
        },
    )

    private fun sendToServer(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        screen: InventoryEditorScreen.Picker,
        purpose: InventoryPickerPurpose.SendToServer,
    ): PickerSpec<InventoryEditorScreen> {
        val names = context.liveData.servers().map { it.name }

        fun apply(server: String) {
            val canonical = PickerOptions.canonical(names, server)
            editClicks(context, player, config, purpose.slots, purpose.interaction) { it.sendToServer = canonical }
            context.back(player)
        }

        return PickerSpec(
            title = Ui.title("Pick a Server", config.id),
            header = PickerHeader(
                "COMPASS",
                "Which Server?",
                clickedLines(purpose.slots, purpose.interaction, "Players go there when they") +
                    PickerHeader.now(currentAction(config, purpose.slots, purpose.interaction)?.sendToServer),
            ),
            empty = EmptyState("No Servers Online", listOf("<bd>Start a server first, or type", "<bd>the name of one below.")),
            screen = screen,
            options = PickerOptions.servers(
                context.liveData.servers(),
                currentAction(config, purpose.slots, purpose.interaction)?.sendToServer,
            ),
            onPick = ::apply,
            onTyped = { typed ->
                PickerOptions.match(names, typed, "No server by that name.").also { result ->
                    if (result == null) apply(typed)
                }
            },
        )
    }

    private fun sounds(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        screen: InventoryEditorScreen.Picker,
        purpose: InventoryPickerPurpose.Sound,
    ): PickerSpec<InventoryEditorScreen> {
        val session = context.session(player)
        val catalog = context.catalogs.sounds()
        val filter = (session.pickerResult as? PickerResult.Filtered)?.typed
        val action = currentAction(config, purpose.slots, purpose.interaction)
        val options = action?.playSoundOptions ?: SoundTuning.DEFAULTS

        fun pick(sound: String) {
            val canonical = PickerOptions.canonical(catalog, sound)
            val filtered = session.pickerResult is PickerResult.Filtered
            editClicks(context, player, config, purpose.slots, purpose.interaction) { it.playSound = canonical }
            if (filtered) {
                context.render(player, screen)
            } else {
                val result = PickerResult.Added(canonical, "Tune volume and pitch below, or go Back.")
                PickerTemplate.showResult(context, player, screen, result)
            }
        }

        return PickerSpec(
            title = Ui.title("Pick a Sound", config.id),
            header = PickerHeader(
                "NOTE_BLOCK",
                "Which Sound?",
                clickedLines(purpose.slots, purpose.interaction, "Plays when a player") +
                    listOf(PickerHeader.now(action?.playSound), "<hnt>Right-click a sound to hear it."),
            ),
            empty = EmptyState("No Sounds Found", listOf("<bd>Type the key of a sound below,", "<bd>e.g. entity.player.levelup.")),
            screen = screen,
            options = PickerOptions.sounds(catalog, filter, action?.playSound),
            onPick = ::pick,
            typeHint = PickerOptions.SOUND_TYPE_HINT,
            onTyped = { typed ->
                PickerOptions.search(catalog, typed, "sound key") ?: run {
                    pick(typed)
                    null
                }
            },
            onRightClick = { option -> player.playSound(option.value, options) },
            gridRows = 2,
            extras = { pane ->
                SoundTuning.place(pane, player, action?.playSound, options) { updated ->
                    val changed = context.change(player, config.id) { fresh ->
                        val items = fresh.items.filter { it.slot in purpose.slots }
                        val actions = items.mapNotNull { ItemClickActions.of(it, purpose.interaction) }
                        if (actions.none { it.playSound != null }) return@change null
                        actions.forEach { it.playSoundOptions = updated(it.playSoundOptions) }
                        fresh
                    }
                    if (changed) context.render(player, screen)
                }
            },
            filteredNoun = "sounds",
        )
    }

    private fun materialLines(purpose: InventoryPickerPurpose, current: String?): List<String> = when (purpose) {
        InventoryPickerPurpose.NewItem -> listOf("<bd>The new item sticks to your", "<bd>cursor; click a slot to place it.")

        is InventoryPickerPurpose.LookMaterial -> listOf(
            "<bd>What entries of <val>${purpose.group} <bd>show while",
            "<bd>they are ${StateLooksMenuBuilder.label(purpose.state).lowercase()}.",
            "",
            PickerHeader.now(if (current == null) null else Ui.pretty(current)),
        )

        is InventoryPickerPurpose.Material -> listOf(
            "<bd>What the item looks like in the menu.",
            "",
            "<bd>Editing: <val>" + if (purpose.slots.size == 1) "This slot" else "${purpose.slots.size} slots",
            if (current == null) "<bd>Now: <warn>Mixed" else PickerHeader.now(Ui.pretty(current)),
        )

        else -> emptyList()
    }

    private fun clickedLines(slots: List<Int>, interaction: PlayerInteraction, lead: String): List<String> {
        val item = if (slots.size == 1) "this item" else "these ${slots.size} items"
        val verb = if (lead.endsWith("they")) Clicks.verb(interaction).removeSuffix("s") else Clicks.verb(interaction)
        return listOf("<bd>$lead", "<bd>$verb $item.", "")
    }

    private fun currentAction(
        config: InventoryConfiguration,
        slots: List<Int>,
        interaction: PlayerInteraction,
    ): ActionConfiguration? =
        config.items.firstOrNull { it.slot in slots }?.let { ItemClickActions.of(it, interaction) }

    private fun editClicks(
        context: InventoryEditorContext,
        player: NpcPlayer,
        config: InventoryConfiguration,
        slots: List<Int>,
        interaction: PlayerInteraction,
        edit: (ActionConfiguration) -> Unit,
    ) {
        context.change(player, config.id) { fresh ->
            fresh.items.filter { it.slot in slots }.forEach { edit(ItemClickActions.orCreate(it, interaction)) }
            fresh
        }
    }

    private const val RECENT_FALLBACK = 27
}
