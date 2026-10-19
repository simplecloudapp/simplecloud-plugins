plugins {
    id("simplecloud.base-conventions")
}

version = "0.0.6"

dependencies {
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.slf4j.api)
    implementation(libs.adventure)
    implementation(libs.adventure.text.minimessage)
    implementation(projects.core.coreShared)
    implementation(libs.bundles.configurate)
    implementation(libs.bundles.cloud)
}
