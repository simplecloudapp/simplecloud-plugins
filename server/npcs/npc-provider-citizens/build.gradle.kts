plugins {
    id("simplecloud.npc-conventions")
}

dependencies {
    implementation(projects.server.npcs.npcProviderBase)
    compileOnly(libs.npc.paper.api)
    compileOnly(libs.citizens.main) {
        exclude(group = "net.byteflux")
    }
}
