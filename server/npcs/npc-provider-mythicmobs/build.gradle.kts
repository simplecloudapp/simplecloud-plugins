plugins {
    id("simplecloud.npc-conventions")
}

dependencies {
    implementation(projects.server.npcs.npcProviderBase)
    compileOnly(libs.npc.paper.api)
    compileOnly(libs.mythic.dist)
    testImplementation(libs.mythic.dist)
}
