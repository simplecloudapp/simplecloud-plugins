plugins {
    id("simplecloud.base-conventions")
}

dependencies {
    api(projects.core.coreShared)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.bundles.adventure)
    compileOnly(libs.slf4j.api)
    implementation(libs.bundles.configurate)
    implementation(libs.bundles.cloud)

    testImplementation(libs.simplecloud.api)
    testImplementation(libs.bundles.adventure)
    testImplementation(libs.slf4j.api)
}
