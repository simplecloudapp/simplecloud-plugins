plugins {
    id("simplecloud.base-conventions")
}

dependencies {
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.slf4j.api)
    implementation(libs.adventure)
    implementation(libs.adventure.text.minimessage)
    implementation(projects.core.coreShared)
    implementation(libs.bundles.configurate)
    implementation(libs.bundles.cloud)
}
