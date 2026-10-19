import dev.detekt.gradle.Detekt

plugins {
    id("dev.detekt")
}

detekt {
    buildUponDefaultConfig = true
    parallel = true
    config.setFrom(isolated.rootProject.projectDirectory.file(".detekt/detekt.yml"))
}

tasks.withType<Detekt>().configureEach {
    exclude { it.file.invariantSeparatorsPath.contains("/build/generated/") }
}

pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
    tasks.named("check") {
        setDependsOn(dependsOn.filterNot { it is TaskProvider<*> && it.name == "detekt" })
        dependsOn("detektMain", "detektTest")
    }
}
