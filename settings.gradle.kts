pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "plugins"

val depotToken = providers.environmentVariable("DEPOT_TOKEN").orNull

buildCache {
    remote<HttpBuildCache> {
        url = uri("https://cache.depot.dev")
        isEnabled = depotToken != null
        isPush = providers.environmentVariable("CI").isPresent
        credentials {
            username = ""
            password = depotToken
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":core:core-shared",
    ":core:core-paper",
    ":core:core-minestom",
    ":core:core-velocity",
    ":core:core-bungeecord",
)

include(
    ":proxy:notify:notify-shared",
    ":proxy:notify:notify-bungeecord",
    ":proxy:notify:notify-velocity",

    ":proxy:server-connection:connection-shared",
    ":proxy:server-connection:connection-bungeecord",
    ":proxy:server-connection:connection-velocity",
    ":proxy:server-connection:connection-waterdog",

    ":proxy:proxy-essentials:proxy-essentials-shared",
    ":proxy:proxy-essentials:proxy-essentials-bungeecord",
    ":proxy:proxy-essentials:proxy-essentials-velocity",

    ":proxy:command:command-shared",
    ":proxy:command:command-bungeecord",
    ":proxy:command:command-velocity",
)

include(
    ":server:signs:signs-shared",
    ":server:signs:signs-paper",

    ":server:prefixes:prefixes-api",
    ":server:prefixes:prefixes-shared",
    ":server:prefixes:prefixes-paper",
    ":server:prefixes:prefixes-minestom",

    ":server:npcs:npc-api",
    ":server:npcs:npc-shared",
    ":server:npcs:npc-common",
    ":server:npcs:npc-paper",
    ":server:npcs:npc-packetevents",
    ":server:npcs:npc-provider-base",
    ":server:npcs:npc-provider-standalone",
    ":server:npcs:npc-provider-mannequin",
    ":server:npcs:npc-provider-citizens",
    ":server:npcs:npc-provider-fancynpcs",
    ":server:npcs:npc-provider-mythicmobs",
    ":server:npcs:npc-provider-znpcsplus",

    ":server:placeholders:placeholders-shared",
    ":server:placeholders:placeholders-paper",
)
