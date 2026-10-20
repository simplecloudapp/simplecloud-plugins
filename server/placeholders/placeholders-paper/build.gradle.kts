plugins {
    id("simplecloud.base-conventions")
    alias(libs.plugins.blossom)
}

dependencies {
    implementation(projects.server.placeholders.placeholdersShared)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.paper.api)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.miniplaceholders.api)
}

sourceSets {
    main {
        blossom {
            kotlinSources {
                property("version", project.version.toString())
            }
        }
    }
}
