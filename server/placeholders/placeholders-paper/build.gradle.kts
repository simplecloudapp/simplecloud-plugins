plugins {
    id("simplecloud.modrinth-conventions")
}

dependencies {
    implementation(projects.server.placeholders.placeholdersShared)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.paper.api)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.miniplaceholders.api)
}

modrinth {
    projectId.set("U1XWdWNx")
    gameVersions.set(listOf("1.21.11", "26.1", "26.1.1", "26.1.2", "26.2", "26.3"))
    loaders.addAll("paper", "purpur", "folia")
}
