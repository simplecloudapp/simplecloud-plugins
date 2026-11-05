plugins {
    id("simplecloud.publishing-conventions")
}

dependencies {
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.bundles.cloud)
    compileOnly(libs.bundles.adventure)
    compileOnly(libs.bundles.configurate)
    compileOnly(libs.slf4j.api)

    testImplementation(libs.bundles.configurate)
}
