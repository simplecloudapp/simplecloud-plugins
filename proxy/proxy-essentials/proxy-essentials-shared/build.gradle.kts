plugins {
    id("simplecloud.base-conventions")
}

dependencies {
    api(libs.bundles.adventure)
    api(libs.bundles.configurate)
    api(libs.bundles.cloud)
    implementation(projects.core.coreShared)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.slf4j.api)
}
