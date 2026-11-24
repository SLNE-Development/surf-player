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

rootProject.name = "surf-social"

include("surf-social-api")
include("surf-social-core:surf-social-core-common")
include("surf-social-core:surf-social-core-client")
include("surf-social-paper")
include("surf-social-minestom")
include("surf-social-velocity")
include("surf-social-microservice")
include("surf-social-placeholder-expansion")
