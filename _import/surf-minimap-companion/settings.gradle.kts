rootProject.name = "surf-minimap-companion"

include("surf-minimap-companion-api")
include("surf-minimap-companion-core")
include("surf-minimap-companion-paper")
include("surf-minimap-companion-minestom")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://reposilite.slne.dev/releases")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.slne.surf.api.gradle.settings") version "+"
}
