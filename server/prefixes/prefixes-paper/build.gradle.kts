import dev.detekt.gradle.Detekt

plugins {
    id("simplecloud.base-conventions")
    alias(libs.plugins.minotaur)
    alias(libs.plugins.paperweight.userdev)
}

version = "1.1.1"

dependencies {
    implementation(projects.server.prefixes.prefixesShared)
    implementation(projects.core.corePaper)
    implementation(libs.cloud.paper)
    compileOnly(libs.paper.api)
    compileOnly(libs.luckperms.api)
    paperweight.paperDevBundle(libs.versions.paper)
}

sourceSets {
    main {
        kotlin {
            srcDir("../custom-names/custom-names-api/src/main/kotlin")
            srcDir("../custom-names/custom-names-paper/src/main/kotlin")
            exclude("**/CustomNamesPlugin.kt")
        }
    }
}

tasks.withType<Detekt>().configureEach {
    exclude { it.file.invariantSeparatorsPath.contains("/custom-names/") }
}

tasks.shadowJar {
    exclude("google/protobuf/**", "src/google/protobuf/**")
    relocate("com.google.protobuf", "app.simplecloud.plugin.prefixes.libs.protobuf")
    relocate("space.chunks.customname", "app.simplecloud.plugin.prefixes.libs.customname")
}

modrinth {
    token.set(project.findProperty("modrinthToken") as String? ?: System.getenv("MODRINTH_TOKEN"))
    projectId.set("FZ0Sdplu")
    versionNumber.set(project.version.toString())
    versionType.set("release")
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(
        "26.2",
        "26.3",
    )
    loaders.addAll("paper", "purpur", "folia")
    changelog.set("https://docs.simplecloud.app/changelog")
    syncBodyFrom.set(file("../README.md").readText())
}
