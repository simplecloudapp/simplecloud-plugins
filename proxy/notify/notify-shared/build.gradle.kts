plugins {
    id("simplecloud.base-conventions")
}

version = "1.0.0"

dependencies {
    api(libs.bundles.cloud)
    api(projects.core.coreShared)
    api(libs.bundles.configurate)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.adventure)
    compileOnly(libs.adventure.text.minimessage)
    compileOnly(libs.slf4j.api)
}
