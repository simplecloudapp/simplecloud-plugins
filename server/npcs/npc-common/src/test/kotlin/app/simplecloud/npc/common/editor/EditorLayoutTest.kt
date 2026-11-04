package app.simplecloud.npc.common.editor

import app.simplecloud.npc.common.FakePlayer
import app.simplecloud.npc.common.NoHolograms
import app.simplecloud.npc.common.NoPlayers
import app.simplecloud.npc.common.RecordingNpcRenderer
import app.simplecloud.npc.common.editor.canvas.CanvasInput
import app.simplecloud.npc.common.editor.canvas.CanvasSurface
import app.simplecloud.npc.common.editor.canvas.CanvasView
import app.simplecloud.npc.common.editor.core.KnownTarget
import app.simplecloud.npc.common.editor.core.LiveServer
import app.simplecloud.npc.common.editor.inventory.InventoryEditor
import app.simplecloud.npc.common.editor.inventory.InventoryEditorScreen
import app.simplecloud.npc.common.editor.inventory.InventoryPickerPurpose
import app.simplecloud.npc.common.editor.inventory.MenuTemplate
import app.simplecloud.npc.common.editor.inventory.StudioTab
import app.simplecloud.npc.common.editor.menu.EditorMenu
import app.simplecloud.npc.common.editor.menu.MenuClick
import app.simplecloud.npc.common.editor.npc.ConfirmPurpose
import app.simplecloud.npc.common.editor.npc.NpcEditor
import app.simplecloud.npc.common.editor.npc.NpcEditorLiveData
import app.simplecloud.npc.common.editor.npc.NpcEditorScreen
import app.simplecloud.npc.common.editor.npc.NpcTab
import app.simplecloud.npc.common.editor.npc.PickerPurpose
import app.simplecloud.npc.common.editor.npc.SoundField
import app.simplecloud.npc.common.editor.npc.TargetSummary
import app.simplecloud.npc.common.editor.ui.Layout
import app.simplecloud.npc.common.platform.EditorMenuOpener
import app.simplecloud.npc.common.platform.PlatformCatalogs
import app.simplecloud.npc.common.plugin.NpcPluginContext
import app.simplecloud.npc.core.config.NpcConfig
import app.simplecloud.npc.core.interaction.PlayerInteraction
import app.simplecloud.npc.core.inventory.InventoryConfiguration.InventoryItemConfiguration
import app.simplecloud.npc.core.inventory.InventoryRepository
import app.simplecloud.npc.core.platform.NpcPlayer
import app.simplecloud.npc.core.repository.NpcRepository
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertTrue

class EditorLayoutTest {

    private companion object {
        const val CELL = 12
    }

    private val opener = object : EditorMenuOpener {
        var last: EditorMenu? = null

        override fun runSync(task: () -> Unit) = task()
        override fun runLater(delayTicks: Long, task: () -> Unit) = Unit
        override fun open(player: NpcPlayer, menu: EditorMenu, onClick: (Int, MenuClick) -> Unit, onClose: () -> Unit) {
            last = menu
        }

        override fun isShowing(player: NpcPlayer, menu: EditorMenu): Boolean = last === menu
    }

    private val canvas = object : CanvasSurface {
        override fun open(player: NpcPlayer, view: CanvasView, onInput: (CanvasInput) -> Unit, onClose: () -> Unit) = Unit
        override fun detach(player: NpcPlayer) = Unit
        override fun inventoryItem(player: NpcPlayer, index: Int): InventoryItemConfiguration? = null
        override fun equipmentItem(player: NpcPlayer, index: Int): NpcConfig.EquipmentItem? = null
    }

    private val live = object : NpcEditorLiveData {
        override fun summaryOf(target: String) = TargetSummary(isGroup = true, liveServers = 1, players = 3)
        override fun playersInRange(config: NpcConfig) = 2
        override fun joinState(target: String): String? = null
        override fun knownTargets() = listOf(KnownTarget("Lobby", true), KnownTarget("Limbo", true))
        override fun servers() = emptyList<LiveServer>()
    }

    private val context = NpcPluginContext(
        renderer = RecordingNpcRenderer(),
        hologramRenderer = NoHolograms,
        playerDirectory = NoPlayers,
        inventoryRepository = InventoryRepository(createTempDirectory("inventories")),
        npcRepository = NpcRepository(createTempDirectory("npcs")),
        catalogs = object : PlatformCatalogs {
            override fun materials(): List<String> = PlatformCatalogs.DEFAULT.materials()
            override fun sounds(): List<String> = listOf("AMBIENT_CAVE", "BLOCK_NOTE_BLOCK_PLING", "UI_BUTTON_CLICK")
        },
    )

    private val editor = NpcEditor(opener, ChatInputPrompts(), canvas, live) { context }
    private val player = FakePlayer()

    private fun screens(id: String): List<NpcEditorScreen> = NpcTab.entries.map { NpcEditorScreen.Tab(id, it) } + listOf(
        NpcEditorScreen.SkinPicker(id),
        NpcEditorScreen.Glow(id),
        NpcEditorScreen.HologramFrames(id, NpcConfig.DEFAULT_JOIN_STATE, 0),
        NpcEditorScreen.ActionEditor(id, PlayerInteraction.RIGHT_CLICK, NpcConfig.DEFAULT_JOIN_STATE),
        NpcEditorScreen.ActionBlocks(id, PlayerInteraction.RIGHT_CLICK, NpcConfig.DEFAULT_JOIN_STATE),
        NpcEditorScreen.TitleEditor(id, PlayerInteraction.RIGHT_CLICK, NpcConfig.DEFAULT_JOIN_STATE),
        NpcEditorScreen.Picker(id, PickerPurpose.AddTarget),
        NpcEditorScreen.Picker(id, PickerPurpose.AddJoinState(forHologram = true)),
        NpcEditorScreen.Picker(id, PickerPurpose.PickSound(SoundField.Push)),
        NpcEditorScreen.Picker(id, PickerPurpose.PickServer(PlayerInteraction.RIGHT_CLICK, NpcConfig.DEFAULT_JOIN_STATE)),
        NpcEditorScreen.Confirm(id, ConfirmPurpose.RemoveJoinState("maintenance")),
    )

    private fun render(screen: NpcEditorScreen): EditorMenu? {
        editor.open(player, requireNotNull(context.npcRepository.find("limbo")))
        if (screen != NpcEditorScreen.Tab("limbo", NpcTab.NPC)) editor.navigate(player, screen)
        return opener.last
    }

    private fun grid(menu: EditorMenu): String = (0 until menu.size / 9).joinToString("\n") { row ->
        (0 until 9).joinToString(" ") { column ->
            val item = menu.elements[row * 9 + column]?.item
            (item?.material?.take(CELL) ?: ".").padEnd(CELL, ' ')
        }
    }

    private val inventories = InventoryEditor(opener, ChatInputPrompts(), canvas) { context }

    private fun check(menu: EditorMenu, problems: MutableList<String>) {
        if (menu.size != Layout.SIZE) problems += "${menu.title}: ${menu.size} slots instead of ${Layout.SIZE}"
        val controls = menu.elements.keys.count { it / 9 == 5 }
        menu.elements.keys.forEach { slot ->
            val row = slot / 9
            val column = slot % 9
            val edge = column == 0 || column == 8
            when {
                row == 1 -> problems += "${menu.title}: slot $slot in the empty second row"

                row in 2..4 && edge -> problems += "${menu.title}: slot $slot outside the inner seven columns"

                row == 0 && edge -> problems += "${menu.title}: slot $slot in a top corner"

                row == 5 && column in listOf(1, 2, 6, 7) && controls <= 3 ->
                    problems += "${menu.title}: tool at slot $slot is not next to the middle"
            }
        }
    }

    @Test
    fun `every editor menu follows the shared layout`() {
        context.npcRepository.save(
            NpcConfig(
                id = "limbo",
                targetServers = mutableListOf("Limbo"),
                pushback = NpcConfig.PushbackConfiguration(enabled = true),
            ),
        )
        context.inventoryRepository.save(MenuTemplate.SERVER_SELECTOR.build("example"))
        val menu = requireNotNull(context.inventoryRepository.find("example"))

        val report = StringBuilder()
        val details = StringBuilder()
        val problems = mutableListOf<String>()
        fun record(name: String, rendered: EditorMenu?, checked: Boolean = true) {
            val shown = rendered ?: return
            report.append("== ${shown.title}  ($name)\n").append(grid(shown)).append("\n\n")
            details.append("== ${shown.title}  ($name)\n")
            shown.elements.toSortedMap().forEach { (slot, element) ->
                val item = element.item
                val glint = if (item.glowing) " (glint)" else ""
                details.append("  slot $slot  ${item.material}$glint  ${item.name}\n")
                item.lore.forEach { line -> details.append("      | $line\n") }
            }
            details.append("\n")
            if (checked) check(shown, problems)
        }

        screens("limbo").forEach { screen ->
            record(screen::class.simpleName.orEmpty(), render(screen), checked = screen !is NpcEditorScreen.Confirm)
        }

        inventories.openCreate(player)
        record("Templates", opener.last)
        inventoryScreens(menu.id).forEach { screen ->
            inventories.open(player, menu)
            if (screen != InventoryEditorScreen.Hub(menu.id)) inventories.navigate(player, screen)
            record(screen::class.simpleName.orEmpty(), opener.last)
        }

        File("build/editor-layouts.txt").writeText(report.toString())
        File("build/editor-layouts-detail.txt").writeText(details.toString())
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    private fun inventoryScreens(id: String): List<InventoryEditorScreen> {
        val slots = listOf(19)
        val click = PlayerInteraction.LEFT_CLICK
        return listOf(
            InventoryEditorScreen.Hub(id),
            InventoryEditorScreen.Studio(id, slots),
            InventoryEditorScreen.Studio(id, slots, StudioTab.CLICK),
            InventoryEditorScreen.Studio(id, slots, StudioTab.LIVE),
            InventoryEditorScreen.Lore(id, slots),
            InventoryEditorScreen.ItemAction(id, slots, click),
            InventoryEditorScreen.ItemActionBlocks(id, slots, click),
            InventoryEditorScreen.ItemTitle(id, slots, click),
            InventoryEditorScreen.LiveGroup(id, "servers"),
            InventoryEditorScreen.StateLooks(id, "servers"),
            InventoryEditorScreen.StateLook(id, "servers", "FULL"),
            InventoryEditorScreen.Picker(id, InventoryPickerPurpose.Material(slots)),
            InventoryEditorScreen.Picker(id, InventoryPickerPurpose.NewLiveGroup),
            InventoryEditorScreen.Picker(id, InventoryPickerPurpose.LiveSourceType("servers")),
            InventoryEditorScreen.Picker(id, InventoryPickerPurpose.LiveSourceGroup("servers")),
            InventoryEditorScreen.Picker(id, InventoryPickerPurpose.UsedBy),
            InventoryEditorScreen.Picker(id, InventoryPickerPurpose.OpenInventory(slots, click)),
            InventoryEditorScreen.Picker(id, InventoryPickerPurpose.SendToServer(slots, click)),
            InventoryEditorScreen.Picker(id, InventoryPickerPurpose.Sound(slots, click)),
        )
    }
}
