plugins {
    id("simplecloud.base-conventions")
}

version = "0.0.6"

dependencies {
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.slf4j.api)
    implementation(libs.adventure)
    implementation(libs.adventure.text.minimessage)
    implementation(libs.simplecloud.plugin)
    implementation(libs.bundles.configurate)
    implementation(libs.bundles.cloud)
}
