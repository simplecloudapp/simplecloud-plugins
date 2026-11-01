import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("simplecloud.base-conventions")
}

repositories {
    exclusive("https://repo.codemc.io/repository/maven-public/", "com.github.retrooper")
    exclusive("https://maven.pvphub.me/tofaa", "io.github.tofaa2")
    exclusive("https://repo.pyr.lol/snapshots", "lol.pyr")
    exclusive("https://maven.citizensnpcs.co/repo", "net.citizensnpcs")
    exclusive("https://repo.fancyinnovations.com/releases", "de.oliver")
    exclusive("https://mvn.lumine.io/repository/maven-public/", "io.lumine")
}

fun RepositoryHandler.exclusive(url: String, vararg groups: String) = exclusiveContent {
    forRepository { maven(url) }
    filter { groups.forEach(::includeGroup) }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

val serverProvidedAdventure = setOf(
    "adventure-api",
    "adventure-key",
    "adventure-text-minimessage",
    "adventure-text-serializer-commons",
    "adventure-text-serializer-gson",
    "adventure-text-serializer-json",
    "adventure-text-serializer-legacy",
    "adventure-text-serializer-plain",
    "adventure-text-logger-slf4j",
)
val adventureFloor = the<LibrariesForLibs>().versions.npc.adventure.floor.get()

configurations.matching {
    it.name == "compileClasspath" || it.name.endsWith("CompileClasspath")
}.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "net.kyori" && requested.name in serverProvidedAdventure) {
            val target = findProperty("npc.adventureVersion")?.toString() ?: adventureFloor
            useVersion(target)
            because("the paper-api this module compiles against provides Adventure $target")
        }
    }
}

detekt {
    baseline = file("detekt-baseline.xml")
}
