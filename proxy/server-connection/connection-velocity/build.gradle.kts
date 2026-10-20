plugins {
    id("simplecloud.base-conventions")
    kotlin("kapt")
    alias(libs.plugins.minotaur)
}

version = "1.0.10"

dependencies {
    implementation(projects.proxy.serverConnection.connectionShared)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.velocity.api)
    compileOnly(libs.slf4j.api)
    kapt(libs.velocity.api)
}

modrinth {
    token.set(project.findProperty("modrinthToken") as String? ?: System.getenv("MODRINTH_TOKEN"))
    projectId.set("4Vw4Mgla")
    versionNumber.set(project.version.toString())
    versionType.set("release")
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(
        "1.20",
        "1.20.1",
        "1.20.2",
        "1.20.3",
        "1.20.4",
        "1.20.5",
        "1.20.6",
        "1.21",
        "1.21.1",
        "1.21.2",
        "1.21.3",
        "1.21.4",
        "1.21.5",
        "1.21.6",
        "1.21.7",
        "1.21.8",
        "1.21.9",
        "1.21.10",
        "1.21.11",
        "26.1",
        "26.1.1",
        "26.1.2",
        "26.2",
        "26.3",
    )
    loaders.add("velocity")
    changelog.set("https://docs.simplecloud.app/changelog")
    syncBodyFrom.set(file("../README.md").readText())
}
