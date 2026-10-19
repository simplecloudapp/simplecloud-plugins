plugins {
    id("dev.detekt")
}

detekt {
    buildUponDefaultConfig = true
    parallel = true
    config.setFrom(isolated.rootProject.projectDirectory.file("config/detekt/detekt.yml"))
}

pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
    tasks.named("check") {
        setDependsOn(dependsOn.filterNot { it is TaskProvider<*> && it.name == "detekt" })
        dependsOn("detektMain", "detektTest")
    }
}
