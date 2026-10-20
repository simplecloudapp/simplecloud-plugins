plugins {
    id("simplecloud.base-conventions")
    `maven-publish`
}

java {
    withSourcesJar()
}

shadow {
    addShadowVariantIntoJavaComponent = false
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = project.name
            pom {
                name = project.name
                url = "https://github.com/simplecloudapp/simplecloud-plugins"
                scm {
                    url = "https://github.com/simplecloudapp/simplecloud-plugins"
                    connection = "scm:git:https://github.com/simplecloudapp/simplecloud-plugins.git"
                }
            }
        }
    }
    repositories {
        maven {
            name = "simplecloud"
            url = uri("https://repo.simplecloud.app/snapshots")
            credentials {
                username = providers.environmentVariable("SIMPLECLOUD_USERNAME").orNull
                password = providers.environmentVariable("SIMPLECLOUD_PASSWORD").orNull
            }
        }
    }
}
