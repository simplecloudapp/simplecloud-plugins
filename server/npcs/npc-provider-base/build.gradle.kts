plugins {
    id("simplecloud.npc-conventions")
}

dependencies {
    api(projects.server.npcs.npcShared)
    compileOnly(libs.npc.paper.api)
    compileOnly(project(projects.server.npcs.npcPacketevents.path, "shadowRuntimeElements"))
    testImplementation(libs.npc.paper.api)
    testImplementation(project(projects.server.npcs.npcPacketevents.path, "shadowRuntimeElements"))
}
