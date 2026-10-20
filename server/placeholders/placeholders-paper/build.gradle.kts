plugins {
    id("simplecloud.base-conventions")
}

dependencies {
    implementation(projects.server.placeholders.placeholdersShared)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.paper.api)
    compileOnly(libs.placeholderapi)
}
