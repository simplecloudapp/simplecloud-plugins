plugins {
    id("simplecloud.npc-conventions")
}

dependencies {
    implementation(projects.server.npcs.npcProviderBase)
    compileOnly(libs.npc.paper.api)
    compileOnly(project(projects.server.npcs.npcPacketevents.path, "shadowRuntimeElements"))
    testImplementation(project(projects.server.npcs.npcPacketevents.path, "shadowRuntimeElements"))
}
