package app.simplecloud.npc.common.editor.npc.appearance

import app.simplecloud.npc.common.editor.core.back
import app.simplecloud.npc.common.editor.core.pageKey
import app.simplecloud.npc.common.editor.core.pager
import app.simplecloud.npc.common.editor.menu.EditorMenu
import app.simplecloud.npc.common.editor.menu.Paginator
import app.simplecloud.npc.common.editor.menu.Pane
import app.simplecloud.npc.common.editor.npc.NpcEditorContext
import app.simplecloud.npc.common.editor.npc.NpcEditorScreen
import app.simplecloud.npc.common.editor.npc.NpcFormat
import app.simplecloud.npc.common.editor.ui.Layout
import app.simplecloud.npc.common.editor.ui.Ui
import app.simplecloud.npc.common.manager.NpcOperationResult
import app.simplecloud.npc.common.text.sendError
import app.simplecloud.npc.common.text.sendInfo
import app.simplecloud.npc.common.utils.BackgroundTasks
import app.simplecloud.npc.common.utils.SkinInput
import app.simplecloud.npc.core.config.NpcConfig
import app.simplecloud.npc.core.platform.NpcPlayer

object SkinMenuBuilder {
    private const val SIZE = Layout.SIZE

    private const val RESET_SLOT = Layout.TOOL_RIGHT
    private const val INPUT_SLOT = Layout.TOOL_LEFT
    private const val EMPTY_SLOT = 31

    fun build(context: NpcEditorContext, player: NpcPlayer, config: NpcConfig): EditorMenu {
        val session = context.session(player)
        val online =
            context.playerDirectory.onlinePlayers().sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        val current = config.entity.skin.sourcePlayer
        val screen = NpcEditorScreen.SkinPicker(config.id)
        val pages = Paginator(session.pages, pageKey(screen), online, Layout.CONTENT.size)
        val grid = Layout.list(pages.visible.size)
        val pane = Pane(SIZE)

        pages.visible.forEachIndexed { index, onlinePlayer ->
            val selected = onlinePlayer.name.equals(current, true)
            pane.left(
                grid[index],
                Ui.head(
                    if (selected) "<val>${onlinePlayer.name}" else "<ttl>${onlinePlayer.name}",
                    listOf(if (selected) "<on>In use" else "<key>Left-click to use this skin"),
                    owner = onlinePlayer.uniqueId,
                    glowing = selected,
                ),
            ) { applySkin(context, player, config, context.renderer.captureSkin(onlinePlayer)) }
        }

        pane[Layout.HEADER] = Ui.head(
            "<ttl>Current Skin",
            listOf("<bd>Now: <val>${NpcFormat.skinLabel(config.entity.skin)}"),
            texture = config.entity.skin.texture,
            signature = config.entity.skin.signature,
        )
        if (online.isEmpty()) {
            pane[EMPTY_SLOT] = Ui.item(
                "LIGHT_GRAY_DYE",
                "<off>No Players Online",
                listOf("<bd>Type a username or UUID below", "<bd>to use that player's skin."),
            )
        }

        pane.back(context, player)
        pane.pager(pages, listOf("<bd>Players online: <val>${online.size}")) {
            context.render(player, screen)
        }

        pane.left(
            RESET_SLOT,
            Ui.item("ARMOR_STAND", "<ttl>Default Skin", listOf("<key>Left-click to use the default skin")),
        ) { applySkin(context, player, config, NpcConfig.SkinConfiguration()) }
        pane.left(
            INPUT_SLOT,
            Ui.item("OAK_SIGN", "<ttl>Type a Name", listOf("<bd>Any username or UUID.", "", "<key>Left-click to type it in chat")),
        ) { promptSkin(context, player, config) }

        return pane.menu(Ui.title("Skin", NpcFormat.displayName(config)))
    }

    private fun promptSkin(context: NpcEditorContext, player: NpcPlayer, config: NpcConfig) {
        context.textPrompts.open(
            player,
            title = "Skin",
            instruction = "Type the username or UUID of the player whose skin to use.",
            current = config.entity.skin.sourcePlayer,
            validate = { input ->
                when (SkinInput.parse(input)) {
                    null -> "That is no username or UUID."
                    is SkinInput.Texture -> "Set texture skins in the NPC's config file."
                    else -> null
                }
            },
        ) { text ->
            when (val parsed = SkinInput.parse(text)) {
                is SkinInput.Uuid -> lookUp(context, player, config, text) {
                    context.renderer.captureSkinByUuid(parsed.uuid)
                }

                is SkinInput.Username -> lookUp(context, player, config, text) {
                    context.renderer.captureSkinByUsername(parsed.name)?.copy(sourcePlayer = parsed.name)
                }

                is SkinInput.Texture, null -> context.render(player, NpcEditorScreen.SkinPicker(config.id))
            }
        }
    }

    private fun lookUp(
        context: NpcEditorContext,
        player: NpcPlayer,
        config: NpcConfig,
        label: String,
        fetch: suspend () -> NpcConfig.SkinConfiguration?,
    ) {
        player.sendInfo("Looking up the skin of {}.", label)

        BackgroundTasks.launch {
            val skin = fetch()
            context.runSync {
                if (skin == null) {
                    player.sendError("Could not find a skin for {}.", label)
                    context.render(player, NpcEditorScreen.SkinPicker(config.id))
                } else {
                    applySkin(context, player, config, skin)
                }
            }
        }
    }

    private fun applySkin(
        context: NpcEditorContext,
        player: NpcPlayer,
        config: NpcConfig,
        skin: NpcConfig.SkinConfiguration,
    ) {
        val stillHere = context.isShowing(player) { it is NpcEditorScreen.SkinPicker && it.npcId == config.id }

        if (config.entity.provider !in context.providers.fixedSkinProviders) {
            player.sendError(
                "NPC {} uses provider '{}', which mirrors each viewer's own skin automatically, " +
                    "so a fixed skin can't be set for it.",
                config.id,
                config.entity.provider,
            )

            if (stillHere) context.back(player)

            return
        }

        when (context.npcManager.updateSkin(config.id, skin)) {
            is NpcOperationResult.Success -> if (stillHere) context.back(player)

            is NpcOperationResult.Failure -> {
                context.reportMissing(player, config.id)
                if (stillHere) context.close(player)
            }
        }
    }
}
