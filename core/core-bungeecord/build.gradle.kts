plugins {
    id("simplecloud.base-conventions")
}

dependencies {
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.bundles.cloud)
    compileOnly(libs.bundles.adventure)
    compileOnly(libs.bundles.configurate)
    compileOnly(libs.slf4j.api)

    compileOnly(projects.core.coreShared)
    compileOnly(libs.cloud.bungee)
    compileOnly(libs.bungeecord.api)
    compileOnly(libs.adventure.text.serializer.gson)
}
