plugins {
    id("simplecloud.base-conventions")
}

dependencies {
    api(projects.core.coreShared)
    implementation(libs.bundles.adventure)
    implementation(libs.bundles.configurate)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.slf4j.api)
}
