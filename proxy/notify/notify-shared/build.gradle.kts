plugins {
    id("simplecloud.base-conventions")
}

dependencies {
    api(libs.bundles.cloud)
    api(projects.core.coreShared)
    api(libs.bundles.configurate)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.adventure)
    compileOnly(libs.adventure.text.minimessage)
    compileOnly(libs.slf4j.api)

    testImplementation(libs.simplecloud.api.testing)
    testImplementation(libs.bundles.adventure)
    testImplementation(libs.slf4j.api)
}
