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

/**
 * Projects keep their artifact names (`surf-player-*`) while living in short directories.
 */
fun module(name: String, directory: String) {
    include(name)
    project(name).projectDir = file(directory)
}

module(":surf-player-api", "api")
module(":surf-player-core", "core")
module(":surf-player-core:surf-player-core-common", "core/core-common")
module(":surf-player-core:surf-player-core-client", "core/core-client")
module(":surf-player-paper", "runtime/paper")
module(":surf-player-minestom", "runtime/minestom")
module(":surf-player-velocity", "runtime/velocity")
module(":surf-player-microservice", "runtime/microservice")
module(":surf-player-placeholder-expansion", "runtime/placeholder-expansion")
