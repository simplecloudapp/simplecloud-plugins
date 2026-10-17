import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.gradleup.shadow")
}

group = "app.simplecloud.plugin"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://buf.build/gen/maven")
    maven("https://repo.simplecloud.app/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://repo.spectr.is/snapshots")
    maven("https://repo.waterdog.dev/releases/")
    maven("https://repo.waterdog.dev/snapshots/")
    maven("https://repo.opencollab.dev/maven-releases/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
}

dependencies {
    implementation(libs.kotlin.coroutines)
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

tasks.shadowJar {
    mergeServiceFiles()
    archiveFileName.set("${project.name}.jar")
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(
            "version" to project.version
        )
    }
}
