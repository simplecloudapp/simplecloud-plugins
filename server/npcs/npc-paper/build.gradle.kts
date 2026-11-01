plugins {
    id("simplecloud.npc-conventions")
    id("simplecloud.modrinth-conventions")
}

dependencies {
    compileOnly(libs.npc.paper.api)

    implementation(libs.cloud.paper)

    compileOnly(libs.npc.simplecloud.api)
    implementation(project(projects.server.npcs.npcPacketevents.path, "shadowRuntimeElements"))

    implementation(projects.server.npcs.npcApi)
    implementation(projects.server.npcs.npcCommon)
    implementation(projects.server.npcs.npcProviderBase)
    implementation(projects.server.npcs.npcProviderStandalone)
    implementation(projects.server.npcs.npcProviderCitizens)
    implementation(projects.server.npcs.npcProviderFancynpcs)
    implementation(projects.server.npcs.npcProviderMythicmobs)
    implementation(projects.server.npcs.npcProviderZnpcsplus)

    runtimeOnly(projects.server.npcs.npcProviderMannequin)

    testImplementation(libs.npc.paper.mannequin.api)
}

tasks.shadowJar {
    val prefix = "app.simplecloud.npc.relocate"
    relocate("kotlin", "$prefix.kotlin")
    relocate("kotlinx", "$prefix.kotlinx")
    relocate("_COROUTINE", "$prefix._COROUTINE")
    relocate("org.incendo.cloud", "$prefix.cloud")
    relocate("org.spongepowered.configurate", "$prefix.configurate")
    relocate("io.leangen.geantyref", "$prefix.geantyref")
    relocate("app.simplecloud.plugin", "$prefix.simplecloud.plugin")
    relocate("io.github.retrooper.packetevents", "$prefix.packetevents")
    relocate("com.github.retrooper.packetevents", "$prefix.packetevents.api")
    relocate("me.tofaa.entitylib", "$prefix.entitylib")
    relocate("net.kyori.option", "$prefix.option")

    dependencies {
        exclude(dependency("org.jetbrains:annotations"))
    }

    exclude("META-INF/maven/**")
    exclude("META-INF/services/javax.annotation.processing.Processor")
    exclude("META-INF/gradle/**")
    exclude("META-INF/com.android.tools/**")
    exclude("META-INF/proguard/**")
    exclude("META-INF/*.version")
    exclude("META-INF/*.kotlin_module")
    exclude("**/module-info.class")
    exclude("DebugProbesKt.bin")
}

val floorAdventure = libs.versions.npc.adventure.floor.get()
val serverAdventureVersions = listOf(floorAdventure, "4.24.0", "5.2.0")
val serverAdventureClasspaths = serverAdventureVersions.associateWith { version ->
    configurations.create("serverAdventure${version.replace(".", "")}") {
        isCanBeConsumed = false
        isCanBeResolved = true
    }.also { configuration ->
        dependencies {
            configuration(libs.npc.paper.api) { isTransitive = false }
            listOf(
                "adventure-api",
                "adventure-text-minimessage",
                "adventure-text-serializer-gson",
                "adventure-text-serializer-legacy",
                "adventure-text-serializer-plain",
            ).forEach { configuration("net.kyori:$it:$version") }
        }
    }
}

tasks.test {
    val pluginJar = tasks.shadowJar.flatMap { it.archiveFile }
    inputs.file(pluginJar).withPropertyName("pluginJar")
    serverAdventureClasspaths.forEach { (version, classpath) ->
        inputs.files(classpath).withPropertyName("serverAdventure$version")
    }
    val serverClasspaths = serverAdventureClasspaths.mapValues { (_, classpath) -> classpath.elements }
    doFirst {
        systemProperty("npc.pluginJar", pluginJar.get().asFile.absolutePath)
        serverClasspaths.forEach { (version, classpath) ->
            systemProperty("npc.serverClasspath.$version", classpath.get().joinToString(File.pathSeparator) { it.asFile.absolutePath })
        }
    }
    systemProperty("npc.floorAdventure", floorAdventure)
    systemProperty("npc.gatedClasses.$floorAdventure", "app/simplecloud/npc/provider/mannequin/")
    systemProperty("npc.gatedClasses.4.24.0", "app/simplecloud/npc/provider/mannequin/")
}

modrinth {
    projectId.set("eyNPY9oJ")
    gameVersions.set(
        listOf(
            "1.20.6",
            "1.21",
            "1.21.1",
            "1.21.2",
            "1.21.3",
            "1.21.4",
            "1.21.5",
            "1.21.6",
            "1.21.7",
            "1.21.8",
            "1.21.9",
            "1.21.10",
            "1.21.11",
            "26.1",
            "26.1.1",
            "26.1.2",
            "26.2",
            "26.3",
        ),
    )
    loaders.addAll("paper", "purpur")
}
