package app.simplecloud.npc.common.editor.core

import app.simplecloud.npc.common.editor.menu.EditorMenu
import app.simplecloud.npc.common.editor.menu.MenuClick
import app.simplecloud.npc.common.editor.menu.Pane
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.item.NpcItem
import app.simplecloud.npc.core.platform.NpcPlayer

class ConfirmSpec<S : Any>(
    val title: String,
    val screen: S,
    val verb: String,
    val head: NpcItem,
    val fire: () -> Unit,
)

object ConfirmGate {
    private const val SIZE = 27

    private const val HEAD_SLOT = 4
    private const val KEEP_SLOT = 11
    private const val FIRE_SLOT = 15

    fun <S : Any> build(host: EditorHost<S>, player: NpcPlayer, spec: ConfirmSpec<S>): EditorMenu {
        val pane = Pane(SIZE)

        pane[HEAD_SLOT] = spec.head
        pane.left(KEEP_SLOT, Ui.item("LIME_CONCRETE", "<on>Keep It", listOf("<key>Left-click to go back"))) {
            host.back(player)
        }
        pane.left(
            FIRE_SLOT,
            Ui.item("RED_CONCRETE", "<err>${spec.verb}", listOf("<key>Left-click to ${spec.verb.lowercase()} it")),
        ) { spec.fire() }

        return pane.menu(spec.title)
    }
}

object Arming {
    const val WINDOW_MILLIS = 5_000L
    private const val TICKS_PER_SECOND = 20L

    fun remainingSeconds(session: EditorSession<*>): Long =
        ((session.armedDeadlineMillis - System.currentTimeMillis() + 999) / 1000).coerceAtLeast(0)

    fun <S : Any> arm(host: EditorHost<S>, player: NpcPlayer, screen: S, key: String) {
        val serial = host.session(player).arm(key, WINDOW_MILLIS)
        host.render(player, screen)
        countdown(host, player, screen, key, serial)
    }

    private fun <S : Any> countdown(host: EditorHost<S>, player: NpcPlayer, screen: S, key: String, serial: Int) {
        host.runLater(TICKS_PER_SECOND) {
            val session = host.sessions.peek(player.uniqueId) ?: return@runLater
            if (session.armedSerial != serial || session.armedKey != key) return@runLater
            if (!host.isShowing(player) { it == screen }) return@runLater

            val expired = System.currentTimeMillis() >= session.armedDeadlineMillis
            if (expired) session.disarm()
            host.render(player, screen)
            if (!expired) countdown(host, player, screen, key, serial)
        }
    }
}

fun <S : Any> Pane.armed(
    slot: Int,
    host: EditorHost<S>,
    player: NpcPlayer,
    screen: S,
    key: String,
    idle: NpcItem,
    verb: String,
    fire: () -> Unit,
) {
    val session = host.session(player)
    val item = if (session.isArmed(key)) {
        Ui.item(
            "TNT",
            "<err>Click Again",
            listOf("<warn>${Arming.remainingSeconds(session)}s left", "", "<key>Left-click to ${verb.lowercase()}"),
            glowing = true,
        )
    } else {
        idle
    }

    left(slot, item) {
        if (session.isArmed(key)) {
            session.disarm()
            fire()
        } else {
            Arming.arm(host, player, screen, key)
        }
    }
}
