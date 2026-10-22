import groovy.json.JsonSlurper
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

plugins {
    id("simplecloud.base-conventions")
    id("com.modrinth.minotaur")
}

val pluginName = path.substringBeforeLast(':').substringAfterLast(':')

val isSelectedPlugin = providers.gradleProperty("plugin")
    .map { it == pluginName }
    .getOrElse(false)

modrinth {
    token.set(providers.gradleProperty("modrinthToken").orElse(providers.environmentVariable("MODRINTH_TOKEN")))
    versionNumber.set(project.version.toString())
    versionType.set("release")
    uploadFile.set(tasks.named("shadowJar"))
    gameVersions.addAll(
        "1.20",
        "1.20.1",
        "1.20.2",
        "1.20.3",
        "1.20.4",
        "1.20.5",
        "1.20.6",
        "1.21",
        "1.21.1",
        "1.21.2",
        "1.21.3",
        "1.21.4",
        "1.21.5",
        "1.21.6",
        "1.21.7",
        "1.21.8",
        "1.21.9",
        "1.21.10",
        "1.21.11",
        "26.1",
        "26.1.1",
        "26.1.2",
        "26.2",
        "26.3",
    )
    changelog.set("https://docs.simplecloud.app/changelog")
    syncBodyFrom.set(file("../README.md").readText())
}

val modrinthProjectId: Property<String> = modrinth.projectId
val modrinthVersionNumber: Property<String> = modrinth.versionNumber
val modrinthLoaders: ListProperty<String> = modrinth.loaders

tasks.named("modrinth") {
    onlyIf("version is not published on Modrinth yet") {
        val loaders = URLEncoder.encode(modrinthLoaders.get().joinToString(",", "[", "]") { "\"$it\"" }, Charsets.UTF_8)
        val request = HttpRequest.newBuilder(URI("https://api.modrinth.com/v2/project/${modrinthProjectId.get()}/version?loaders=$loaders"))
            .header("User-Agent", "simplecloudapp/simplecloud-plugins")
            .build()
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        check(response.statusCode() == 200) { "Failed to fetch Modrinth versions: ${response.statusCode()} ${response.body()}" }
        val versions = JsonSlurper().parseText(response.body()) as List<*>
        versions.none { (it as Map<*, *>)["version_number"] == modrinthVersionNumber.get() }
    }
}

tasks.register("publishModrinth") {
    group = "publishing"
    description = "Publishes this module to Modrinth if it belongs to the plugin given by -Pplugin."
    if (isSelectedPlugin) {
        dependsOn("modrinth", "modrinthSyncBody")
    }
}
