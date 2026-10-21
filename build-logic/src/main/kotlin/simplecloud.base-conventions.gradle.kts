import app.simplecloud.gradle.CopyPluginJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

// expose version catalog
val Project.libs: LibrariesForLibs
    get() = the<LibrariesForLibs>()

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.gradleup.shadow")
    id("net.kyori.blossom")
    id("simplecloud.spotless-conventions")
    id("simplecloud.detekt-conventions")
}

group = "app.simplecloud.plugin"
version = providers.gradleProperty("version.${path.substringBeforeLast(':').substringAfterLast(':')}").get()

repositories {
    mavenCentral()
    maven("https://buf.build/gen/maven")
    maven("https://repo.simplecloud.app/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.spectr.is/snapshots")
    maven("https://repo.waterdog.dev/releases/")
    maven("https://repo.waterdog.dev/snapshots/")
    maven("https://repo.opencollab.dev/maven-releases/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    if (!name.endsWith("-api")) {
        implementation(libs.kotlin.coroutines)
    }
    testImplementation(libs.kotlin.test)
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        jvmTarget = JvmTarget.JVM_25
        languageVersion = KotlinVersion.KOTLIN_2_4
        apiVersion = KotlinVersion.KOTLIN_2_4
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.test {
    useJUnitPlatform()
}

sourceSets {
    main {
        blossom {
            kotlinSources {
                property("version", provider { project.version.toString() })
            }
        }
    }
}

tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()
    filesNotMatching(listOf("META-INF/services/**", "META-INF/*.kotlin_module")) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    archiveFileName.set("${project.name}.jar")
}

val platforms = setOf("velocity", "bungeecord", "waterdog", "paper", "minestom")

if (!path.startsWith(":core:") && name.substringAfterLast('-') in platforms) {
    val destination = isolated.rootProject.projectDirectory
        .file("build/plugins/${name.substringBeforeLast('-')}/$name.jar")

    val copyPluginJar = tasks.register<CopyPluginJar>("copyPluginJar") {
        group = "build"
        description = "Copies the plugin jar to build/plugins of the root project."
        jar.set(tasks.shadowJar.flatMap { it.archiveFile })
        this.destination.set(destination)
    }

    tasks.shadowJar {
        finalizedBy(copyPluginJar)
    }
}

// IntelliJ shit
val ideaSyncActive = providers.systemProperty("idea.sync.active").isPresent

tasks.processResources {
    val properties = mapOf("version" to project.version.toString())
    inputs.properties(properties)
    if (!ideaSyncActive) {
        filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
            expand(properties)
        }
    }
}
