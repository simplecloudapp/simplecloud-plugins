plugins {
    id("simplecloud.base-conventions")
    kotlin("kapt")
}

dependencies {
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.bundles.cloud)
    compileOnly(libs.bundles.adventure)
    compileOnly(libs.bundles.configurate)
    compileOnly(libs.slf4j.api)

    compileOnly(projects.core.coreShared)
    compileOnly(libs.cloud.velocity)
    compileOnly(libs.velocity.api)
    kapt(libs.velocity.api)
}
