plugins {
    id("simplecloud.base-conventions")
    kotlin("kapt")
    id("simplecloud.modrinth-conventions")
}

dependencies {
    implementation(projects.proxy.serverConnection.connectionShared)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.velocity.api)
    compileOnly(libs.slf4j.api)
    kapt(libs.velocity.api)
}

modrinth {
    projectId.set("4Vw4Mgla")
    loaders.add("velocity")
}
