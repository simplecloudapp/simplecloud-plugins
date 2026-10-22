plugins {
    id("simplecloud.base-conventions")
    kotlin("kapt")
    id("simplecloud.modrinth-conventions")
}

dependencies {
    implementation(projects.proxy.command.commandShared)
    implementation(projects.core.coreVelocity)
    implementation(libs.cloud.velocity)
    compileOnly(libs.velocity.api)
    kapt(libs.velocity.api)
}

modrinth {
    projectId.set("qZsNJQis")
    loaders.add("velocity")
}
