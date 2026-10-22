plugins {
    id("simplecloud.base-conventions")
    id("simplecloud.modrinth-conventions")
}

dependencies {
    implementation(projects.proxy.serverConnection.connectionShared)
    implementation(projects.core.coreBungeecord)
    implementation(libs.bundles.adventure)
    implementation(libs.adventure.text.serializer.gson)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.bungeecord.api)
    compileOnly(libs.slf4j.api)
}

modrinth {
    projectId.set("4Vw4Mgla")
    loaders.addAll("bungeecord", "waterfall")
}
