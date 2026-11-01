plugins {
    id("simplecloud.modrinth-conventions")
}

dependencies {
    implementation(projects.server.signs.signsShared)
    implementation(projects.core.corePaper)
    implementation(libs.cloud.paper)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.paper.api)
}

modrinth {
    projectId.set("M2XJERK4")
    gameVersions.set(listOf("26.1", "26.1.1", "26.1.2", "26.2", "26.3"))
    loaders.addAll("paper", "purpur")
}
