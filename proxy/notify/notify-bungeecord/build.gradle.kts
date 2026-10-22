plugins {
    id("simplecloud.base-conventions")
    id("simplecloud.modrinth-conventions")
}

dependencies {
    api(projects.proxy.notify.notifyShared)
    implementation(projects.core.coreBungeecord)
    compileOnly(libs.bungeecord.api)
    compileOnly(libs.slf4j.api)
    implementation(libs.adventure.text.serializer.gson)
    implementation(libs.adventure)
    implementation(libs.adventure.text.minimessage)
    implementation(libs.cloud.bungee)
}

modrinth {
    projectId.set("daugEY3o")
    loaders.addAll("bungeecord", "waterfall")
}
