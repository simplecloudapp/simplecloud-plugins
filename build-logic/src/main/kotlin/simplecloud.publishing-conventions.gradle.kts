import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

plugins {
    id("simplecloud.base-conventions")
    `maven-publish`
}

val repositoryUrl = "https://repo.simplecloud.app/snapshots"

java {
    withSourcesJar()
}

shadow {
    addShadowVariantIntoJavaComponent = false
}

val isSelectedPlugin = providers.gradleProperty("plugin")
    .map { it == path.substringBeforeLast(':').substringAfterLast(':') }
    .getOrElse(false)

// core has no release of its own, so it is published with the plugins that depend on it
val coreDependencies = provider {
    listOf("api", "implementation")
        .flatMap { configurations[it].dependencies.withType<ProjectDependency>() }
        .map { it.path }
        .filter { it.startsWith(":core:") }
        .map { "$it:publish" }
}

tasks.register("publishPlugin") {
    group = "publishing"
    description = "Publishes this module if it belongs to the plugin given by -Pplugin."
    if (isSelectedPlugin) {
        dependsOn(tasks.named("publish"))
        dependsOn(coreDependencies)
    }
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
            url = uri(repositoryUrl)
            credentials {
                username = providers.environmentVariable("SIMPLECLOUD_USERNAME").orNull
                password = providers.environmentVariable("SIMPLECLOUD_PASSWORD").orNull
            }
        }
    }
}

if (path.startsWith(":core:")) {
    val pomUrl = "$repositoryUrl/${group.toString().replace('.', '/')}/$name/$version/$name-$version.pom"

    tasks.withType<PublishToMavenRepository>().configureEach {
        onlyIf("$pomUrl is not published yet") {
            val request = HttpRequest.newBuilder(URI(pomUrl))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build()
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 404
        }
    }
}
