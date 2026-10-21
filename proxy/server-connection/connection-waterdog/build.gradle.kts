plugins {
    id("simplecloud.base-conventions")
}

dependencies {
    implementation(projects.proxy.serverConnection.connectionShared)
    implementation(libs.bundles.adventure)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.waterdog.api)
    compileOnly(libs.slf4j.api)
}
