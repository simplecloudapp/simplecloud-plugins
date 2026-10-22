plugins {
    id("simplecloud.base-conventions")
    kotlin("kapt")
    id("simplecloud.modrinth-conventions")
}

dependencies {
    implementation(projects.proxy.proxyEssentials.proxyEssentialsShared)
    implementation(libs.cloud.velocity)
    implementation(projects.core.coreShared)
    implementation(projects.core.coreVelocity)
    compileOnly(libs.velocity.api)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.slf4j.api)
    kapt(libs.velocity.api)
}

modrinth {
    projectId.set("WzfN2mLK")
    loaders.add("velocity")
}
