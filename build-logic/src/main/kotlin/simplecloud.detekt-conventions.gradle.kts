plugins {
    id("dev.detekt")
}

detekt {
    buildUponDefaultConfig = true
    parallel = true
    config.setFrom(isolated.rootProject.projectDirectory.file(".detekt/detekt.yml"))
}

// generated sources (e.g. blossom templates) are not ours to lint
tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
    exclude { it.file.invariantSeparatorsPath.contains("/build/generated/") }
}

pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
    tasks.named("check") {
        setDependsOn(dependsOn.filterNot { it is TaskProvider<*> && it.name == "detekt" })
        dependsOn("detektMain", "detektTest")
    }
}
