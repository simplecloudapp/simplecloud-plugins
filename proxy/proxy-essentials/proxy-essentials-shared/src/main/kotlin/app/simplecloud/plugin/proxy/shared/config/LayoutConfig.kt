package app.simplecloud.plugin.proxy.shared.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class LayoutConfig(
    val configVersion: String = "2",
    val motd: MotdConfig = MotdConfig(),
    val serverIcon: ServerIconConfig = ServerIconConfig(),
    val playerList: PlayerListConfig = PlayerListConfig(),
    val version: VersionConfig = VersionConfig(),
)

@ConfigSerializable
data class MotdConfig(
    val enabled: Boolean = true,
    val updateType: MotdUpdateType = MotdUpdateType.RANDOM,
    val updateTime: Long? = null,
    val layouts: List<MotdEntry> = listOf(MotdEntry()),
)

@ConfigSerializable
data class MotdEntry(
    val name: String = "default",
    val line1: String = "<center><brand>SimpleCloud</center>",
    val line2: String = "<center><muted>ᴀ ɴᴇᴛᴡᴏʀᴋ ᴘᴏᴡᴇʀᴇᴅ ʙʏ ꜱɪᴍᴘʟᴇᴄʟᴏᴜᴅ.ᴀᴘᴘ</center>",
)

enum class MotdUpdateType {
    RANDOM,
    QUEUE,
}

@ConfigSerializable
data class ServerIconConfig(
    val enabled: Boolean = true,
    val file: String = "simplecloud.png",
)

@ConfigSerializable
data class PlayerListConfig(
    val enabled: Boolean = true,
    val playerList: List<String> = listOf("", "<muted>ꜱɪᴍᴘʟᴇᴄʟᴏᴜᴅ.ᴀᴘᴘ", ""),
)

@ConfigSerializable
data class VersionConfig(
    val name: VersionNameConfig = VersionNameConfig(),
    val slots: SlotsConfig = SlotsConfig(),
)

@ConfigSerializable
data class VersionNameConfig(
    val enabled: Boolean = false,
    val text: String = "Example",
)

@ConfigSerializable
data class SlotsConfig(
    val enabled: Boolean = true,
    val type: MaxPlayerDisplayType = MaxPlayerDisplayType.REAL,
    val fakeSlots: Int = 100,
    val dynamicPlayerRange: Int = 5,
) {
    fun resolveMaxPlayers(onlinePlayers: Int, realMaxPlayers: Int): Int {
        if (!enabled) return realMaxPlayers

        return when (type) {
            MaxPlayerDisplayType.REAL -> realMaxPlayers
            MaxPlayerDisplayType.FAKE -> fakeSlots
            MaxPlayerDisplayType.DYNAMIC -> onlinePlayers + dynamicPlayerRange
        }
    }
}

enum class MaxPlayerDisplayType {
    REAL,
    FAKE,
    DYNAMIC,
}
