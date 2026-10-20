plugins {
    id("simplecloud.base-conventions")
}

dependencies {
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.slf4j.api)
    implementation(libs.caffeine)
}
