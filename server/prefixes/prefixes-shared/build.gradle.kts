plugins {
    id("simplecloud.base-conventions")
    alias(libs.plugins.protobuf)
}

version = "1.1.1"

dependencies {
    api(projects.server.prefixes.prefixesApi)
    api(projects.core.coreShared)
    compileOnly(libs.simplecloud.api)
    compileOnly(libs.bundles.adventure)
    compileOnly(libs.adventure.text.serializer.gson)
    compileOnly(libs.slf4j.api)
    compileOnly(libs.luckperms.api)
    implementation(libs.jnats) {
        exclude(group = "org.bouncycastle")
    }
    implementation(libs.protobuf.kotlin.lite)
    implementation(libs.bundles.configurate)
    implementation(libs.bundles.cloud)
}

sourceSets {
    main {
        kotlin {
            srcDir("build/generated/source/proto/main/kotlin")
        }
        java {
            srcDir("build/generated/source/proto/main/java")
        }
    }
}

protobuf {
    protoc {
        artifact = libs.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach {
            it.builtins {
                named("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

configurations.all {
    resolutionStrategy {
        force("com.google.protobuf:protobuf-javalite:${libs.versions.protobuf.kotlin.get()}")
    }
}
