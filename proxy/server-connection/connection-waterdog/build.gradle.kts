plugins {
    id("simplecloud.base-conventions")
}

version = "1.0.10"

dependencies {
    implementation(projects.proxy.serverConnection.connectionShared)
    implementation(libs.bundles.adventure)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.waterdog.api)
    compileOnly(libs.slf4j.api)
}

tasks.shadowJar {
    relocate("org.spongepowered", "app.simplecloud.plugin.connection.shaded.spongepowered")
    relocate("app.simplecloud.plugin.api", "app.simplecloud.plugin.connection.shaded.plugin.api")
}
