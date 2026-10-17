plugins {
    id("simplecloud.base-conventions")
}

version = "1.0.10"

dependencies {
    api(libs.simplecloud.plugin)
    implementation(libs.bundles.adventure)
    implementation(libs.bundles.configurate)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.slf4j.api)
}

tasks.shadowJar {
    relocate("org.spongepowered", "app.simplecloud.plugin.connection.shaded.spongepowered")
    relocate("app.simplecloud.plugin.api", "app.simplecloud.plugin.connection.shaded.plugin.api")
}
