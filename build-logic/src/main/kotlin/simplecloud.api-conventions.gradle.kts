import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("simplecloud.publishing-conventions")
}

kotlin {
    explicitApi()

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()
}

tasks.check {
    dependsOn(tasks.matching { it.name.startsWith("check") && it.name.endsWith("Abi") })
}
