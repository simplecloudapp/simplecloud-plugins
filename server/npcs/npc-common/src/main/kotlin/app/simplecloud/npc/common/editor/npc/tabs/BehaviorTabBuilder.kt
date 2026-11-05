package app.simplecloud.npc.common.editor.npc.tabs

import app.simplecloud.npc.common.editor.menu.EditorMenu
import app.simplecloud.npc.common.editor.menu.MenuClick
import app.simplecloud.npc.common.editor.npc.NpcEditorContext
import app.simplecloud.npc.common.editor.npc.NpcEditorScreen
import app.simplecloud.npc.common.editor.npc.NpcTab
import app.simplecloud.npc.common.editor.npc.PickerPurpose
import app.simplecloud.npc.common.editor.npc.SoundField
import app.simplecloud.npc.common.editor.ui.Stepper
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.core.config.NpcConfig
import app.simplecloud.npc.core.platform.NpcPlayer

object BehaviorTabBuilder {
    private const val LOOK_AT_SLOT = 21
    private const val LOOK_AT_RANGE_SLOT = 22
    private const val RENDER_RANGE_SLOT = 23
    private const val PUSH_SLOT = 29
    private const val PUSH_STRENGTH_SLOT = 30
    private const val PUSH_RADIUS_SLOT = 31
    private const val UPWARD_BOOST_SLOT = 32
    private const val PUSH_SOUND_SLOT = 33

    private val ENTITY_DEFAULTS = NpcConfig.NpcEntityConfiguration()
    private val PUSH_DEFAULTS = NpcConfig.PushbackConfiguration()

    private val LOOK_AT_RANGE = Stepper(
        label = "Look Range",
        hints = listOf("<bd>Players closer than this are looked at."),
        material = "SCULK_SENSOR",
        range = NpcConfig.NpcEntityConfiguration.LOOK_AT_PLAYER_DISTANCE_RANGE,
        step = 0.5,
        default = ENTITY_DEFAULTS.lookAtPlayerDistance,
        decimals = 1,
        unit = "blocks",
    )
    private val RENDER_RANGE = Stepper(
        label = "Render Range",
        hints = listOf("<bd>How far away players see it."),
        material = "SPYGLASS",
        range = NpcConfig.NpcEntityConfiguration.VIEW_DISTANCE_RANGE,
        step = 4.0,
        default = ENTITY_DEFAULTS.viewDistance,
        decimals = 0,
        unit = "blocks",
    )
    private val PUSH_STRENGTH = Stepper(
        label = "Strength",
        hints = listOf("<bd>How hard it pushes players back."),
        material = "SLIME_BLOCK",
        range = NpcConfig.PushbackConfiguration.STRENGTH_RANGE,
        step = 0.05,
        default = PUSH_DEFAULTS.strength,
        decimals = 2,
    )
    private val PUSH_RADIUS = Stepper(
        label = "Radius",
        hints = listOf("<bd>Players closer than this are pushed."),
        material = "TARGET",
        range = NpcConfig.PushbackConfiguration.RADIUS_RANGE,
        step = 0.5,
        default = PUSH_DEFAULTS.radius,
        decimals = 1,
        unit = "blocks",
    )
    private val UPWARD_BOOST = Stepper(
        label = "Upward Boost",
        hints = listOf("<bd>How high it lifts pushed players."),
        material = "FEATHER",
        range = NpcConfig.PushbackConfiguration.VERTICAL_RANGE,
        step = 0.05,
        default = PUSH_DEFAULTS.vertical,
        decimals = 2,
    )

    fun build(context: NpcEditorContext, player: NpcPlayer, config: NpcConfig): EditorMenu {
        val entity = config.entity
        val pushback = config.pushback
        val screen = NpcEditorScreen.Tab(config.id, NpcTab.BEHAVIOR)
        val pane = TabFrame.pane(context, player, config, NpcTab.BEHAVIOR)

        fun editEntity(mutate: (NpcConfig.NpcEntityConfiguration) -> NpcConfig.NpcEntityConfiguration) =
            context.editEntity(player, config.id, screen, mutate)

        fun editPushback(mutate: (NpcConfig.PushbackConfiguration) -> Unit) =
            context.commit(player, config.id, screen) { fresh -> fresh.also { mutate(it.pushback) } }

        pane.left(LOOK_AT_SLOT, Ui.toggle("Look at Player", entity.lookAtPlayer, material = "ENDER_EYE")) {
            editEntity { it.copy(lookAtPlayer = !it.lookAtPlayer) }
        }
        if (entity.lookAtPlayer) {
            pane[LOOK_AT_RANGE_SLOT] = LOOK_AT_RANGE.element(entity.lookAtPlayerDistance) { value ->
                editEntity { it.copy(lookAtPlayerDistance = value) }
            }
        } else {
            pane[LOOK_AT_RANGE_SLOT] = LOOK_AT_RANGE.disabled(listOf("<hnt>Turn on Look at Player first."))
        }
        pane[RENDER_RANGE_SLOT] = RENDER_RANGE.element(entity.viewDistance) { value ->
            editEntity { it.copy(viewDistance = value) }
        }

        pane.on(
            PUSH_SLOT,
            Ui.toggle(
                "Push",
                pushback.enabled,
                material = null,
                actions = tryActions(pushback.enabled),
            ),
        ) { click ->
            when (click) {
                MenuClick.LEFT -> editPushback { it.enabled = !it.enabled }

                MenuClick.RIGHT -> if (pushback.enabled) {
                    player.push(entity.location, pushback.strength, pushback.vertical)
                    pushback.sound?.let { player.playSound(it, pushback.soundOptions) }
                }

                else -> Unit
            }
        }

        if (!pushback.enabled) {
            val hint = listOf("<hnt>Turn on Push first.")
            pane[PUSH_STRENGTH_SLOT] = PUSH_STRENGTH.disabled(hint)
            pane[PUSH_RADIUS_SLOT] = PUSH_RADIUS.disabled(hint)
            pane[UPWARD_BOOST_SLOT] = UPWARD_BOOST.disabled(hint)
            pane[PUSH_SOUND_SLOT] = Ui.disabled("Push Sound", hint, "NOTE_BLOCK")
        } else {
            pane[PUSH_STRENGTH_SLOT] = PUSH_STRENGTH.element(pushback.strength) { value ->
                editPushback { it.strength = value }
            }
            pane[PUSH_RADIUS_SLOT] = PUSH_RADIUS.element(pushback.radius) { value ->
                editPushback { it.radius = value }
            }
            pane[UPWARD_BOOST_SLOT] = UPWARD_BOOST.element(pushback.vertical) { value ->
                editPushback { it.vertical = value }
            }

            val sound = pushback.sound
            pane.on(
                PUSH_SOUND_SLOT,
                Ui.item(
                    "NOTE_BLOCK",
                    "<ttl>Push Sound",
                    listOf(
                        sound?.let { "<val>$it" } ?: "<off>None",
                        if (sound == null) "<key>Left-click to choose one" else "<key>Left-click to change · Right-click to turn off",
                    ),
                    glowing = sound != null,
                ),
            ) { click ->
                when (click) {
                    MenuClick.LEFT -> context.navigate(
                        player,
                        NpcEditorScreen.Picker(config.id, PickerPurpose.PickSound(SoundField.Push)),
                    )

                    MenuClick.RIGHT -> if (sound != null) editPushback { it.sound = null }

                    else -> Unit
                }
            }
        }

        return TabFrame.menu(pane, config, NpcTab.BEHAVIOR)
    }

    private fun tryActions(enabled: Boolean): List<String> =
        if (enabled) listOf("<key>Right-click to test it on yourself") else emptyList()
}
