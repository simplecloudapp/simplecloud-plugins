plugins {
    id("simplecloud.base-conventions")
    id("simplecloud.modrinth-conventions")
}

dependencies {
    implementation(projects.proxy.command.commandShared)
    implementation(projects.core.coreBungeecord)
    implementation(libs.adventure.text.serializer.gson)
    implementation(libs.cloud.bungee)
    compileOnly(libs.bungeecord.api)
}

modrinth {
    projectId.set("qZsNJQis")
    loaders.addAll("bungeecord", "waterfall")
}
