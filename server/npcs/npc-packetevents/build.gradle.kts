plugins {
    id("simplecloud.npc-conventions")
}

dependencies {
    implementation(libs.packetevents.spigot)
    implementation(libs.entitylib.spigot)
    implementation(libs.bundles.npc.packet.adventure)

    constraints {
        implementation(libs.entitylib.api) { version { strictly(libs.versions.entitylib.get()) } }
        implementation(libs.entitylib.common) { version { strictly(libs.versions.entitylib.get()) } }
    }
}

configurations.runtimeClasspath {
    exclude(group = "com.google.code.gson")
    exclude(group = "org.jetbrains.kotlin")
    exclude(group = "org.jetbrains.kotlinx")
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("net.kyori", "app.simplecloud.npc.relocate.packets.kyori")
    dependencies {
        exclude(dependency("org.jetbrains:annotations"))
    }
    exclude("**/module-info.class")
    exclude("META-INF/maven/**")
}

tasks.jar { enabled = false }
