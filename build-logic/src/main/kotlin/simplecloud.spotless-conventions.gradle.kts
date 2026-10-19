plugins {
    id("com.diffplug.spotless")
}

val ktlintVersion = "1.8.0"
val editorConfig = isolated.rootProject.projectDirectory.file(".editorconfig")

spotless {
    kotlin {
        ktlint(ktlintVersion).setEditorConfigPath(editorConfig)
    }

    kotlinGradle {
        ktlint(ktlintVersion).setEditorConfigPath(editorConfig)
    }
}
