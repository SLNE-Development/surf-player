// Standalone build: automated in-game player tests of surf-player (headless bots against the
// local Docker test network). NOT part of the plugin build in the repository root.
rootProject.name = "surf-player-tests"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        // MCProtocolLib (GeyserMC)
        maven("https://repo.opencollab.dev/main/")
        maven("https://jitpack.io") {
            content { includeGroupByRegex("com\\.github\\..*") }
        }
    }
}
