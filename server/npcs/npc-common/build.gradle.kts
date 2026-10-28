plugins {
    id("simplecloud.npc-conventions")
}

dependencies {
    api(projects.server.npcs.npcShared)
    api(libs.cloud.core)
    api(libs.cloud.annotations)
    compileOnly(libs.npc.simplecloud.api)
    compileOnly(libs.bundles.npc.adventure)

    testImplementation(libs.npc.simplecloud.api)
    testImplementation(libs.bundles.npc.adventure)
    testImplementation(testFixtures(projects.server.npcs.npcShared))
}
