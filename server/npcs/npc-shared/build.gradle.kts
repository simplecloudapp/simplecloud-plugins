plugins {
    id("simplecloud.npc-conventions")
    `java-test-fixtures`
}

dependencies {
    api(libs.configurate.yaml)
    api(libs.npc.simplecloud.plugin)
    compileOnly(libs.npc.simplecloud.api)
    compileOnly(libs.npc.adventure.api)
    compileOnly(libs.npc.adventure.text.minimessage)

    testImplementation(libs.npc.simplecloud.api)
    testImplementation(libs.bundles.npc.adventure)
    testFixturesImplementation(libs.npc.simplecloud.api)
    testFixturesImplementation(libs.kotlin.coroutines)
}
