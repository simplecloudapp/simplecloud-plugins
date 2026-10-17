plugins {
    id("simplecloud.base-conventions")
}

version = "1.0.0-beta.1"

dependencies {
    api(libs.bundles.adventure)
    api(libs.bundles.configurate)
    api(libs.bundles.cloud)
    implementation(projects.api.apiShared)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.slf4j.api)
}

tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
