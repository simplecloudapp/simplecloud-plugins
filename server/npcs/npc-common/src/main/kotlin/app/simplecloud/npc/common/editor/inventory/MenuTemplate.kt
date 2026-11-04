package app.simplecloud.npc.common.editor.inventory

import app.simplecloud.npc.common.inventory.source.GroupServersSource
import app.simplecloud.npc.core.config.NpcConfig
import app.simplecloud.npc.core.inventory.InventoryConfiguration
import app.simplecloud.npc.core.inventory.InventoryConfiguration.EntryStates
import app.simplecloud.npc.core.inventory.InventoryConfiguration.InventoryItemConfiguration
import app.simplecloud.npc.core.inventory.InventoryConfiguration.PageControl
import app.simplecloud.npc.core.inventory.InventoryConfiguration.SourceConfiguration
import app.simplecloud.npc.core.inventory.InventoryConfiguration.StateLook

enum class MenuTemplate(val label: String, val icon: String, val description: List<String>) {
    BLANK("Blank", "ITEM_FRAME", listOf("Three empty rows.")),
    SERVER_SELECTOR(
        "Server Selector",
        "COMPASS",
        listOf("Every running server of the NPC's", "target group, with page arrows."),
    ),
    GROUP_OVERVIEW("Group Overview", "CHEST", listOf("One slot per group,", "with its player count.")),
    PERSISTENT_SERVERS("Persistent Servers", "ENDER_CHEST", listOf("One slot per persistent server.")),
    NAVIGATION("Navigation", "OAK_SIGN", listOf("Three fixed buttons that send", "players to a server each.")),
    ;

    fun build(id: String): InventoryConfiguration = when (this) {
        BLANK -> InventoryConfiguration(id = id, rows = 3)

        SERVER_SELECTOR -> serverSelector(id)

        GROUP_OVERVIEW -> liveGrid(
            id,
            "Groups",
            "groups",
            "groups",
            "CHEST",
            "<brand><entry_name>",
            JOIN_LORE,
        )

        PERSISTENT_SERVERS -> liveGrid(
            id,
            "Servers",
            "servers",
            "persistent-servers",
            "ENDER_CHEST",
            "<brand><entry_name>",
            JOIN_LORE,
        )

        NAVIGATION -> navigation(id)
    }

    private companion object {
        val JOIN_LORE = listOf(
            "<muted>Players: <highlight><entry_online_players>/<entry_max_players>",
            "",
            "<interactive>Click to join",
        )

        fun serverSelector(id: String): InventoryConfiguration {
            val content = (10..16) + (19..25)
            val live = content.mapIndexed { index, slot ->
                InventoryItemConfiguration(
                    slot = slot,
                    material = "LIME_CONCRETE",
                    name = "<brand><entry_group_name>-<entry_numerical_id>",
                    lore = mutableListOf(
                        "<muted>Players: <highlight><entry_online_players>/<entry_max_players>",
                        "<muted>State: <highlight><entry_state>",
                        "",
                        "<interactive>Click to join",
                    ),
                    liveGroup = "servers",
                    live = if (index == 0) {
                        SourceConfiguration(
                            type = GroupServersSource.TYPE,
                            group = GroupServersSource.TARGET_GROUP,
                            states = mutableListOf("AVAILABLE", "INGAME"),
                            looks = mutableMapOf(
                                EntryStates.FULL to StateLook(
                                    material = "RED_CONCRETE",
                                    name = "<brand><entry_group_name>-<entry_numerical_id>",
                                    lore = mutableListOf(
                                        "<muted>Players: <highlight><entry_online_players>/<entry_max_players>",
                                        "",
                                        "<error>This server is full.",
                                    ),
                                ),
                                "INGAME" to StateLook(
                                    material = "ORANGE_CONCRETE",
                                    name = "<brand><entry_group_name>-<entry_numerical_id>",
                                    lore = mutableListOf(
                                        "<muted>Players: <highlight><entry_online_players>/<entry_max_players>",
                                        "",
                                        "<warning>A round is running.",
                                    ),
                                ),
                            ),
                        )
                    } else {
                        null
                    },
                    whenEmpty = if (index == 0) {
                        InventoryItemConfiguration(material = "GRAY_DYE", name = "<muted>No Servers Online")
                    } else {
                        null
                    },
                )
            }
            val controls = listOf(
                InventoryItemConfiguration(
                    slot = 36,
                    material = "BLUE_STAINED_GLASS_PANE",
                    name = "<interactive>Previous Page",
                    liveGroup = "servers",
                    pageControl = PageControl.PREVIOUS,
                ),
                InventoryItemConfiguration(
                    slot = 44,
                    material = "BLUE_STAINED_GLASS_PANE",
                    name = "<interactive>Next Page",
                    liveGroup = "servers",
                    pageControl = PageControl.NEXT,
                ),
            )

            return InventoryConfiguration(
                id = id,
                title = "Servers",
                rows = 5,
                items = (live + controls).toMutableList(),
            )
        }

        fun liveGrid(
            id: String,
            title: String,
            group: String,
            sourceType: String,
            material: String,
            name: String,
            lore: List<String>,
        ): InventoryConfiguration {
            val live = (10..16).mapIndexed { index, slot ->
                InventoryItemConfiguration(
                    slot = slot,
                    material = material,
                    name = name,
                    lore = lore.toMutableList(),
                    liveGroup = group,
                    live = SourceConfiguration(type = sourceType).takeIf { index == 0 },
                )
            }

            return InventoryConfiguration(id = id, title = title, rows = 3, items = live.toMutableList())
        }

        fun navigation(id: String): InventoryConfiguration {
            fun button(slot: Int, material: String, label: String, server: String) = InventoryItemConfiguration(
                slot = slot,
                material = material,
                name = "<brand>$label",
                lore = mutableListOf("<interactive>Click to join"),
                actions = mutableListOf(NpcConfig.ActionConfiguration(sendToServer = server)),
            )

            return InventoryConfiguration(
                id = id,
                title = "Navigation",
                rows = 3,
                items = mutableListOf(
                    button(11, "GRASS_BLOCK", "Lobby", "Lobby"),
                    button(13, "DIAMOND_SWORD", "Survival", "Survival"),
                    button(15, "GOLDEN_APPLE", "Minigames", "Minigames"),
                ),
            )
        }
    }
}
