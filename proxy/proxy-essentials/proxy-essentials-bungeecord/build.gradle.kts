plugins {
    id("simplecloud.base-conventions")
    id("simplecloud.modrinth-conventions")
}

dependencies {
    implementation(projects.proxy.proxyEssentials.proxyEssentialsShared)
    implementation(libs.adventure.text.serializer.gson)
    implementation(projects.core.coreShared)
    implementation(projects.core.coreBungeecord)
    implementation(libs.cloud.bungee)
    compileOnly(libs.bungeecord.api)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.slf4j.api)
}

modrinth {
    projectId.set("WzfN2mLK")
    loaders.addAll("bungeecord", "waterfall")
}
