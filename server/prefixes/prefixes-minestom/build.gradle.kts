import dev.detekt.gradle.Detekt

plugins {
    id("simplecloud.base-conventions")
}

version = "1.1.1"

dependencies {
    api(projects.server.prefixes.prefixesShared)
    implementation(projects.core.coreMinestom)
    implementation(libs.cloud.minestom)
    compileOnly(libs.minestom)
    compileOnly(libs.slf4j.api)
    compileOnly(libs.luckperms.api)
}

sourceSets {
    main {
        kotlin {
            srcDir("../custom-names/custom-names-api/src/main/kotlin")
            srcDir("../custom-names/custom-names-minestom/src/main/kotlin")
        }
    }
}

tasks.withType<Detekt>().configureEach {
    exclude { it.file.invariantSeparatorsPath.contains("/custom-names/") }
}
