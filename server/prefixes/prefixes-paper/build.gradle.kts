import dev.detekt.gradle.Detekt

plugins {
    id("simplecloud.publishing-conventions")
    id("simplecloud.modrinth-conventions")
    alias(libs.plugins.paperweight.userdev)
}

dependencies {
    implementation(projects.server.prefixes.prefixesShared)
    implementation(projects.core.corePaper)
    implementation(libs.cloud.paper)
    implementation(libs.reflection.remapper)
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
    projectId.set("FZ0Sdplu")
    gameVersions.set(listOf("26.2", "26.3"))
    loaders.addAll("paper", "purpur", "folia")
}
