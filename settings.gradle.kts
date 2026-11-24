pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://reposilite.slne.dev/public")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.slne.surf.api.gradle.settings") version "+"
}

rootProject.name = "surf-player"

include("surf-player-api")
include("surf-player-core:surf-player-core-common")
include("surf-player-core:surf-player-core-client")
include("surf-player-paper")
include("surf-player-minestom")
include("surf-player-velocity")
include("surf-player-microservice")
include("surf-player-placeholder-expansion")
