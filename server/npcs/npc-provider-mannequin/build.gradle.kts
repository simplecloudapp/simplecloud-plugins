plugins {
    id("simplecloud.npc-conventions")
}

ext["npc.adventureVersion"] = libs.versions.npc.adventure.mannequin.get()

dependencies {
    compileOnly(libs.npc.paper.mannequin.api)

    implementation(projects.server.npcs.npcProviderBase)
}
