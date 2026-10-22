plugins {
    id("simplecloud.base-conventions")
    kotlin("kapt")
    id("simplecloud.modrinth-conventions")
}

dependencies {
    api(projects.proxy.notify.notifyShared)
    implementation(projects.core.coreVelocity)
    compileOnly(libs.velocity.api)
    compileOnly(libs.slf4j.api)
    kapt(libs.velocity.api)
    implementation(libs.cloud.velocity)
}

modrinth {
    projectId.set("daugEY3o")
    loaders.add("velocity")
}
